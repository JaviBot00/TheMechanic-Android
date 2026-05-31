package com.hotguy.workshopmanagement.data.repository

import com.hotguy.workshopmanagement.data.local.TokenDataStore
import com.hotguy.workshopmanagement.data.remote.api.AuthApiService
import com.hotguy.workshopmanagement.data.remote.dto.auth.LoginRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.auth.RefreshRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.auth.toDomain
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.AuthToken
import com.hotguy.workshopmanagement.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Implementación concreta de [AuthRepository].
 *
 * Orquesta tres componentes para gestionar el ciclo completo de autenticación:
 * - [AuthApiService]: hace las llamadas HTTP al backend.
 * - [TokenDataStore]: persiste los tokens cifrados en disco.
 * - [SessionManager]: mantiene el estado de sesión en memoria para la UI.
 *
 * El patrón de envolver la llamada en [runCatching] devuelve un [Result]
 * al dominio sin propagar excepciones. El ViewModel decide cómo mostrar
 * el error al usuario.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenDataStore: TokenDataStore,
    private val sessionManager: SessionManager
) : AuthRepository {

    /**
     * Autentica al usuario, guarda los tokens y actualiza el [SessionManager].
     *
     * Pasos:
     * 1. Llamada HTTP POST /auth/login con las credenciales.
     * 2. Si tiene éxito, mapear el DTO al modelo de dominio [AuthToken].
     * 3. Persistir los tokens en [TokenDataStore] (cifrados).
     * 4. Notificar al [SessionManager] para que la UI reaccione.
     * 5. Devolver el [AuthToken] al Use Case.
     */
    override suspend fun login(username: String, password: String): Result<AuthToken> =
        runCatching {
            val response = authApiService.login(LoginRequestDto(username, password))
            val authToken = response.toDomain()

            // Persistir tokens en disco (cifrados con Keystore)
            tokenDataStore.saveTokens(
                accessToken  = authToken.accessToken,
                refreshToken = authToken.refreshToken
            )

            // Actualizar el estado en memoria para que el NavGraph
            // reaccione y navegue al destino correcto según el rol
            sessionManager.onLoginSuccess(authToken.role)

            authToken
        }

    /**
     * Cierra la sesión del usuario.
     *
     * Intenta notificar al servidor (para revocar el refresh token en BD),
     * pero independientemente del resultado, siempre limpia el estado local.
     * Si la red está caída, el logout local sigue funcionando.
     */
    override suspend fun logout(): Result<Unit> = runCatching {
        // Intentar logout en el servidor; si falla, lo ignoramos —
        // la sesión local se cierra igualmente en el bloque finally implícito
        runCatching { authApiService.logout() }

        // Limpiar tokens del DataStore y estado en memoria
        tokenDataStore.clearTokens()
        sessionManager.onLogout()
    }
}
