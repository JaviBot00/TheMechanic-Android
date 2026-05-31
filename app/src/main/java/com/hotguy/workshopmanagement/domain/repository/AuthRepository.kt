package com.hotguy.workshopmanagement.domain.repository

import com.hotguy.workshopmanagement.domain.model.AuthToken

/**
 * Contrato del repositorio de autenticación.
 *
 * Esta interfaz vive en la capa de dominio y define QUÉ operaciones de
 * autenticación existen, sin decir CÓMO se implementan. La implementación
 * concreta ([AuthRepositoryImpl]) vive en la capa de datos y usa Retrofit
 * y [TokenDataStore] para cumplir este contrato.
 *
 * Los Use Cases dependen de esta interfaz, nunca de la implementación.
 * Esto permite sustituir la implementación en tests sin tocar ningún
 * Use Case ni ViewModel.
 *
 * Todas las funciones son suspend porque implican operaciones de red
 * o de disco (E/S asíncrona). Devuelven [Result] de la stdlib de Kotlin:
 * - [Result.success] con el valor si la operación tuvo éxito.
 * - [Result.failure] con la excepción si algo salió mal (red, servidor, etc.).
 * El llamador decide cómo manejar el error, sin necesidad de try/catch.
 */
interface AuthRepository {

    /**
     * Autentica al usuario con sus credenciales.
     *
     * Si tiene éxito, los tokens se guardan automáticamente en [TokenDataStore]
     * y el [SessionManager] se actualiza con el rol del usuario.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano (solo viaja por HTTPS, nunca se persiste).
     * @return [Result] con el [AuthToken] si el login fue correcto,
     *         o con la excepción si las credenciales son incorrectas o hay error de red.
     */
    suspend fun login(username: String, password: String): Result<AuthToken>

    /**
     * Cierra la sesión del usuario autenticado.
     *
     * Llama al endpoint de logout del servidor (para revocar el refresh token)
     * y limpia los tokens almacenados localmente independientemente del resultado
     * del servidor — si la red falla, la sesión local se cierra igualmente.
     *
     * @return [Result.success] siempre (el logout local siempre funciona).
     */
    suspend fun logout(): Result<Unit>
}
