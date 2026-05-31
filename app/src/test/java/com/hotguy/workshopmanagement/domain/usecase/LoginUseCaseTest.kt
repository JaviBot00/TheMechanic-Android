package com.hotguy.workshopmanagement.domain.usecase

import com.hotguy.workshopmanagement.domain.model.AuthToken
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.repository.AuthRepository
import com.hotguy.workshopmanagement.domain.usecase.auth.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [LoginUseCase].
 *
 * Se prueban las validaciones de dominio (campos vacíos) y la delegación
 * al repositorio cuando las credenciales son válidas. El repositorio se
 * sustituye por un mock de MockK, por lo que estos tests se ejecutan en
 * la JVM sin necesidad de dispositivo ni de Spring Boot corriendo.
 *
 * Patrón AAA (Arrange - Act - Assert) en cada test.
 */
class LoginUseCaseTest {

    // ── Dependencias ──────────────────────────────────────────────────────────

    /** Mock del repositorio — no llama a la red, devuelve lo que le indiquemos. */
    private val authRepository: AuthRepository = mockk()

    /** La clase bajo test, inicializada con el mock. */
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        loginUseCase = LoginUseCase(authRepository)
    }

    // ── Tests de validación de dominio ────────────────────────────────────────

    @Test
    fun `given blank username, when invoke, then returns failure without calling repository`() = runTest {
        // Act
        val result = loginUseCase(username = "", password = "password123")

        // Assert
        assertTrue("El resultado debe ser un fallo", result.isFailure)
        assertTrue(
            "El mensaje debe mencionar el usuario",
            result.exceptionOrNull()?.message?.contains("usuario") == true
        )
        // El repositorio NO debe haberse llamado
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `given blank password, when invoke, then returns failure without calling repository`() = runTest {
        val result = loginUseCase(username = "admin", password = "   ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("contraseña") == true)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `given blank username and password, when invoke, then returns failure for username first`() = runTest {
        val result = loginUseCase(username = "", password = "")

        assertTrue(result.isFailure)
        // La validación del username ocurre antes que la de la contraseña
        assertTrue(result.exceptionOrNull()?.message?.contains("usuario") == true)
    }

    // ── Tests de delegación al repositorio ────────────────────────────────────

    @Test
    fun `given valid credentials, when invoke, then delegates to repository`() = runTest {
        // Arrange
        val expectedToken = AuthToken(
            accessToken  = "access.token.jwt",
            refreshToken = "refresh-uuid",
            role         = UserRole.ADMIN
        )
        coEvery { authRepository.login("admin", "secret123") } returns Result.success(expectedToken)

        // Act
        val result = loginUseCase(username = "admin", password = "secret123")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedToken, result.getOrNull())
        coVerify(exactly = 1) { authRepository.login("admin", "secret123") }
    }

    @Test
    fun `given valid credentials with whitespace, when invoke, then trims username before calling repository`() = runTest {
        // Arrange — el repositorio espera el username sin espacios
        coEvery { authRepository.login("admin", "secret123") } returns Result.success(
            AuthToken("token", "refresh", UserRole.ADMIN)
        )

        // Act — el usuario introdujo espacios al inicio
        val result = loginUseCase(username = "  admin  ", password = "secret123")

        // Assert — se llamó al repositorio con el username limpio
        assertTrue(result.isSuccess)
        coVerify { authRepository.login("admin", "secret123") }
    }

    @Test
    fun `given repository returns failure, when invoke, then propagates failure`() = runTest {
        // Arrange
        val networkError = Exception("HTTP 401 Unauthorized")
        coEvery { authRepository.login(any(), any()) } returns Result.failure(networkError)

        // Act
        val result = loginUseCase(username = "admin", password = "wrongpass")

        // Assert
        assertTrue(result.isFailure)
        assertEquals(networkError, result.exceptionOrNull())
    }
}
