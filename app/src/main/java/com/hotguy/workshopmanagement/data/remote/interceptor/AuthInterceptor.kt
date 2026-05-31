package com.hotguy.workshopmanagement.data.remote.interceptor

import com.hotguy.workshopmanagement.data.local.TokenDataStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor de OkHttp que añade el token JWT a cada petición saliente.
 *
 * OkHttp ejecuta todos los interceptores en cadena antes de enviar la
 * petición al servidor. Este interceptor se sitúa en la cadena de
 * "application interceptors" (los primeros en ejecutarse), de forma que
 * opera sobre la petición original antes de cualquier lógica de red.
 *
 * Responsabilidad única: leer el access token actual y añadir la cabecera
 * Authorization. Nada más. La lógica de refresco de token cuando expira
 * (401) la gestiona [TokenAuthenticator], que es un componente diferente
 * con un contrato distinto en OkHttp.
 *
 * Funcionamiento:
 *   1. Lee el access token de [TokenDataStore] de forma síncrona.
 *      (OkHttp corre en hilos de IO, nunca en el hilo principal.)
 *   2. Si hay token, añade la cabecera "Authorization: Bearer <token>".
 *   3. Si no hay token (usuario no autenticado), deja pasar la petición
 *      sin cabecera. El servidor devolverá 401 para endpoints protegidos.
 *   4. Siempre llama a chain.proceed() para continuar la cadena.
 */
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Leer el token actual de forma síncrona desde DataStore
        val accessToken = tokenDataStore.getAccessTokenSync()

        // Si no hay token, dejamos pasar la petición tal cual.
        // Esto cubre los endpoints públicos (/auth/login, /auth/refresh).
        val request = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
