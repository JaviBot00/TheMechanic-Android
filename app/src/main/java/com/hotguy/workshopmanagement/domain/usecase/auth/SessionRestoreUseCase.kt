package com.hotguy.workshopmanagement.domain.usecase.auth

import android.util.Base64
import com.hotguy.workshopmanagement.data.local.TokenDataStore
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.UserRole
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use Case: restaurar la sesión del usuario al arrancar la aplicación.
 *
 * Al matar y relanzar la app, el [SessionManager] se reinicia vacío
 * (está en memoria). Sin embargo, los tokens siguen persistidos en [TokenDataStore].
 * Este use case lee el access token, extrae el rol de sus claims y restaura
 * el estado de sesión en el [SessionManager] sin necesidad de que el usuario
 * vuelva a hacer login.
 *
 * Se llama desde [MainActivity] en `onCreate`, antes de que el NavGraph
 * decida cuál es la pantalla inicial. De esta forma, cuando [AppNavGraph]
 * lee el [SessionState] por primera vez, ya está correctamente inicializado.
 *
 * Flujo:
 *   App arranca → SessionManager.Unauthenticated
 *       │
 *       ▼
 *   SessionRestoreUseCase.invoke()
 *       │
 *       ├── Leer access token de DataStore
 *       │       │
 *       │       ├── Token encontrado → extraer rol del JWT → onLoginSuccess(role)
 *       │       │         → SessionState = Authenticated → NavGraph → Dashboard/Vehículos
 *       │       │
 *       │       └── Sin token → no hacer nada
 *       │                 → SessionState = Unauthenticated → NavGraph → Login
 *       │
 *       └── Error al descifrar → limpiar tokens corruptos → NavGraph → Login
 *
 * @param tokenDataStore  Almacenamiento cifrado de tokens.
 * @param sessionManager  Gestor de sesión en memoria.
 */
class SessionRestoreUseCase @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val sessionManager: SessionManager
) {
    /**
     * Intenta restaurar la sesión a partir de los tokens persistidos.
     * Si no hay token o el token está corrupto, no hace nada — el usuario
     * verá la pantalla de login.
     */
    suspend operator fun invoke() {
        runCatching {
            // Leer el access token actual del DataStore (desencriptado)
            val accessToken = tokenDataStore.accessTokenFlow.first()
                ?: return   // Sin token → no hay sesión que restaurar

            // Extraer el rol del payload del JWT sin verificar la firma.
            // La verificación la hace el servidor en cada llamada. Aquí solo
            // necesitamos el rol para configurar la UI correctamente.
            val role = extractRoleFromJwt(accessToken) ?: return

            // Restaurar el estado de sesión en memoria
            sessionManager.onLoginSuccess(role)
        }.onFailure {
            // Si los tokens están corruptos (ej. la clave del Keystore fue borrada
            // al reinstalar la app), los limpiamos para evitar un estado inconsistente.
            tokenDataStore.clearTokens()
        }
    }

    /**
     * Extrae el claim "role" del payload de un JWT sin verificar la firma.
     *
     * Un JWT tiene el formato: header.payload.signature
     * El payload es un JSON en Base64Url. Lo decodificamos y buscamos el
     * campo "role" que el backend incluye en cada token.
     *
     * No necesitamos verificar la firma aquí porque:
     * 1. El servidor verificará el token en la primera llamada de red.
     * 2. Solo usamos el rol para decidir qué pantalla mostrar, no para
     *    dar acceso a datos protegidos (eso lo controla el servidor).
     *
     * @param jwt Token JWT en formato header.payload.signature.
     * @return El [UserRole] extraído, o null si el token es inválido.
     */
    private fun extractRoleFromJwt(jwt: String): UserRole? = runCatching {
        // El JWT tiene tres partes separadas por puntos
        val parts = jwt.split(".")
        if (parts.size != 3) return null

        // El payload es la segunda parte, codificada en Base64Url (sin padding)
        val payloadJson = String(
            Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING
            ),
            Charsets.UTF_8
        )

        // Extraer el valor del campo "role" con una búsqueda simple de cadena.
        // Evitamos importar una librería JSON solo para esto — el payload es
        // un JSON simple y el campo "role" siempre tiene el mismo formato.
        val roleRegex = Regex(""""role"\s*:\s*"([^"]+)"""")
        val roleValue = roleRegex.find(payloadJson)?.groupValues?.get(1)
            ?: return null

        UserRole.fromJwtClaim(roleValue)
    }.getOrNull()
}
