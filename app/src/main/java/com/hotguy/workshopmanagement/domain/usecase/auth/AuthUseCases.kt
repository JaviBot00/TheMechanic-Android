package com.hotguy.workshopmanagement.domain.usecase.auth

import com.hotguy.workshopmanagement.domain.model.AuthToken
import com.hotguy.workshopmanagement.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use Case: autenticar al usuario con sus credenciales.
 *
 * La convención `operator fun invoke` permite llamar al use case como si
 * fuera una función directamente, sin necesidad de nombrar el método:
 *   val result = loginUseCase(username, password)
 * en lugar de:
 *   val result = loginUseCase.execute(username, password)
 *
 * Esta clase no contiene lógica de negocio compleja — su valor está en:
 * 1. Ser el punto de entrada único para la operación de login.
 * 2. Poder ser reemplazada por un fake en tests de ViewModel sin tocar
 *    ninguna implementación de red ni de almacenamiento.
 * 3. Ser el lugar donde añadir validaciones de dominio en el futuro
 *    (ej. bloquear login si hay demasiados intentos fallidos).
 *
 * @param authRepository Repositorio de autenticación inyectado por Hilt.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Ejecuta el caso de uso de login.
     *
     * @param username Nombre de usuario introducido en el formulario.
     * @param password Contraseña en texto plano (solo viaja por HTTPS).
     * @return [Result.success] con el [AuthToken] si el login fue correcto.
     *         [Result.failure] con la excepción si las credenciales son
     *         incorrectas o hay un problema de red.
     */
    suspend operator fun invoke(username: String, password: String): Result<AuthToken> {
        // Validación de dominio: no enviar credenciales vacías al servidor.
        // Aunque el formulario ya valida esto en la UI, la regla de negocio
        // vive aquí para que sea independiente de la capa de presentación.
        if (username.isBlank()) return Result.failure(
            IllegalArgumentException("El nombre de usuario no puede estar vacío")
        )
        if (password.isBlank()) return Result.failure(
            IllegalArgumentException("La contraseña no puede estar vacía")
        )

        return authRepository.login(username.trim(), password)
    }
}

/**
 * Use Case: cerrar la sesión del usuario autenticado.
 *
 * Revoca el refresh token en el servidor y limpia el estado local
 * (tokens en DataStore y SessionManager en memoria).
 *
 * @param authRepository Repositorio de autenticación inyectado por Hilt.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Ejecuta el cierre de sesión.
     * Siempre devuelve [Result.success] porque el logout local funciona
     * incluso si el servidor no está disponible.
     */
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
