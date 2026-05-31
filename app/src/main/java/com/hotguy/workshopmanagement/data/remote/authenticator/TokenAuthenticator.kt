package com.hotguy.workshopmanagement.data.remote.authenticator

import com.hotguy.workshopmanagement.data.local.TokenDataStore
import com.hotguy.workshopmanagement.data.remote.api.AuthApiService
import com.hotguy.workshopmanagement.data.remote.dto.auth.RefreshRequestDto
import com.hotguy.workshopmanagement.di.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Provider

/**
 * Autenticador de OkHttp que gestiona el refresco automático del token JWT.
 *
 * OkHttp diferencia entre Interceptors y Authenticators:
 * - [AuthInterceptor]: se ejecuta en CADA petición para añadir el token.
 * - [TokenAuthenticator]: se ejecuta SOLO cuando el servidor devuelve 401,
 *   DESPUÉS de que la petición original ya ha fallado.
 *
 * El contrato de [Authenticator.authenticate] es:
 * - Devolver una nueva [Request] con credenciales frescas → OkHttp la reintenta.
 * - Devolver null → OkHttp propaga el 401 al caller sin reintentar.
 *
 * Flujo completo ante un 401:
 *   1. OkHttp llama a [authenticate] con la respuesta 401 original.
 *   2. Leemos el refresh token del DataStore de forma síncrona.
 *   3. Si no hay refresh token → devolvemos null (logout).
 *   4. Comprobamos si ya hemos intentado refrescar para esta petición
 *      (cabecera "X-Retry-After-Refresh") para evitar bucles infinitos.
 *   5. Llamamos al endpoint /auth/refresh de forma síncrona con runBlocking.
 *      (El Authenticator de OkHttp siempre opera de forma síncrona.)
 *   6. Si el refresco tiene éxito → guardamos los nuevos tokens, notificamos
 *      al SessionManager y devolvemos la petición original con el nuevo token.
 *   7. Si el refresco falla → limpiamos tokens, hacemos logout y devolvemos null.
 *
 * Por qué usamos [Provider<Retrofit>] en lugar de [AuthApiService] directamente:
 *   Hilt resolvería una dependencia circular: NetworkModule crea Retrofit usando
 *   TokenAuthenticator, y TokenAuthenticator necesita Retrofit para llamar al
 *   endpoint de refresh. [Provider] rompe el ciclo porque la instancia de Retrofit
 *   se solicita de forma diferida (lazy), solo en el momento en que se necesita.
 *
 * @param tokenDataStore  Acceso al almacenamiento cifrado de tokens.
 * @param sessionManager  Gestor de sesión para emitir el evento de logout.
 * @param retrofitProvider Proveedor lazy de Retrofit para evitar dependencia circular.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val sessionManager: SessionManager,
    private val retrofitProvider: Provider<Retrofit>
) : Authenticator {

    // Nombre de la cabecera que usamos como flag para detectar bucles infinitos.
    // Si la petición que causó el 401 ya lleva esta cabecera, significa que
    // el token refrescado también fue rechazado → el refresh token expiró.
    companion object {
        private const val RETRY_HEADER = "X-Retry-After-Refresh"
    }

    override fun authenticate(route: Route?, response: Response): Request? {

        // Paso 1: si ya intentamos refrescar para esta petición y volvió a fallar,
        // no reintentar. Esto evita un bucle infinito de refreshes.
        if (response.request.header(RETRY_HEADER) != null) {
            handleLogout()
            return null
        }

        // Paso 2: obtener el refresh token actual de DataStore
        val refreshToken = tokenDataStore.getRefreshTokenSync()
        if (refreshToken == null) {
            // No hay refresh token → no hay forma de recuperar la sesión
            handleLogout()
            return null
        }

        // Paso 3: intentar el refresco de forma síncrona
        return runBlocking {
            runCatching {
                // Obtenemos el servicio de autenticación de forma diferida
                // para evitar la dependencia circular con Retrofit
                val authService = retrofitProvider.get()
                    .create(AuthApiService::class.java)

                val refreshResponse = authService.refresh(RefreshRequestDto(refreshToken))

                // Paso 4: guardar los nuevos tokens en DataStore
                tokenDataStore.saveTokens(
                    accessToken  = refreshResponse.accessToken,
                    refreshToken = refreshResponse.refreshToken
                )

                // Paso 5: reconstruir la petición original con el nuevo access token
                // y añadir el flag para detectar posibles bucles
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                    .header(RETRY_HEADER, "true")
                    .build()

            }.getOrElse {
                // El refresco falló (token expirado, revocado, red caída...)
                // → cerrar sesión y no reintentar
                handleLogout()
                null
            }
        }
    }

    /**
     * Limpia los tokens almacenados y notifica al [SessionManager] para que
     * la UI reaccione navegando a la pantalla de login.
     */
    private fun handleLogout() {
        runBlocking {
            tokenDataStore.clearTokens()
        }
        sessionManager.onLogout()
    }
}
