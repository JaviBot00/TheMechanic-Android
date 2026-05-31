package com.hotguy.workshopmanagement.domain.usecase

import com.hotguy.workshopmanagement.data.local.TokenDataStore
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.auth.SessionRestoreUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [SessionRestoreUseCase].
 *
 * El caso más delicado de testear aquí es la extracción del rol del JWT.
 * Usamos JWTs reales (pero con firma inválida a propósito) para verificar
 * que el parsing del payload funciona correctamente sin depender de
 * ninguna librería JWT en el cliente.
 *
 * Estructura de un JWT de prueba:
 *   header.payload.signature
 * El payload se codifica en Base64Url con el claim "role" incluido.
 */
class SessionRestoreUseCaseTest {

    private val tokenDataStore: TokenDataStore = mockk()
    private val sessionManager: SessionManager = mockk(relaxed = true)

    private lateinit var useCase: SessionRestoreUseCase

    // JWTs de prueba con payload real pero firma inválida.
    // Se generaron con: Base64Url({ "role": "ROLE_ADMIN", "sub": "admin" })
    // El claim "role" es lo único que importa para la restauración de sesión.
    private val jwtWithAdminRole =
        "eyJhbGciOiJIUzI1NiJ9." +
        "eyJyb2xlIjoiUk9MRV9BRE1JTiIsInN1YiI6ImFkbWluIn0." +
        "invalid_signature_intentional"

    private val jwtWithMechanicRole =
        "eyJhbGciOiJIUzI1NiJ9." +
        "eyJyb2xlIjoiUk9MRV9NRUNIQUE5SUMiLCJzdWIiOiJtZWMifQ." +
        "invalid_signature_intentional"

    private val jwtWithClientRole =
        "eyJhbGciOiJIUzI1NiJ9." +
        "eyJyb2xlIjoiUk9MRV9DTElFTlQiLCJzdWIiOiJjbGllbnQifQ." +
        "invalid_signature_intentional"

    @Before
    fun setUp() {
        useCase = SessionRestoreUseCase(tokenDataStore, sessionManager)
    }

    // ── Sin token persistido ──────────────────────────────────────────────────

    @Test
    fun `given no stored token, when invoke, then session is not restored`() = runTest {
        // Arrange — no hay token en DataStore
        every { tokenDataStore.accessTokenFlow } returns flowOf(null)

        // Act
        useCase()

        // Assert — el SessionManager no debe haberse actualizado
        verify(exactly = 0) { sessionManager.onLoginSuccess(any()) }
    }

    // ── Con token válido ──────────────────────────────────────────────────────

    @Test
    fun `given token with ROLE_ADMIN claim, when invoke, then restores ADMIN session`() = runTest {
        every { tokenDataStore.accessTokenFlow } returns flowOf(jwtWithAdminRole)

        useCase()

        verify(exactly = 1) { sessionManager.onLoginSuccess(UserRole.ADMIN) }
    }

    @Test
    fun `given token with ROLE_MECHANIC claim, when invoke, then restores MECHANIC session`() = runTest {
        every { tokenDataStore.accessTokenFlow } returns flowOf(jwtWithMechanicRole)

        useCase()

        verify(exactly = 1) { sessionManager.onLoginSuccess(UserRole.MECHANIC) }
    }

    @Test
    fun `given token with ROLE_CLIENT claim, when invoke, then restores CLIENT session`() = runTest {
        every { tokenDataStore.accessTokenFlow } returns flowOf(jwtWithClientRole)

        useCase()

        verify(exactly = 1) { sessionManager.onLoginSuccess(UserRole.CLIENT) }
    }

    // ── Token corrupto ────────────────────────────────────────────────────────

    @Test
    fun `given malformed token with only one part, when invoke, then does not restore session`() = runTest {
        every { tokenDataStore.accessTokenFlow } returns flowOf("not_a_jwt")

        useCase()

        verify(exactly = 0) { sessionManager.onLoginSuccess(any()) }
    }

    @Test
    fun `given malformed token without role claim, when invoke, then does not restore session`() = runTest {
        // JWT con payload válido pero sin claim "role"
        // payload: { "sub": "admin" }
        val jwtWithoutRole =
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbiJ9." +
            "invalid_sig"
        every { tokenDataStore.accessTokenFlow } returns flowOf(jwtWithoutRole)

        useCase()

        verify(exactly = 0) { sessionManager.onLoginSuccess(any()) }
    }

    @Test
    fun `given DataStore throws exception, when invoke, then clears tokens and does not crash`() = runTest {
        // Simula corrupción: el Flow lanza una excepción
        every { tokenDataStore.accessTokenFlow } throws RuntimeException("DataStore corrupted")
        coEvery { tokenDataStore.clearTokens() } returns Unit

        // No debe lanzar excepción al llamador
        useCase()

        // Los tokens corruptos deben haberse limpiado
        coVerify(exactly = 1) { tokenDataStore.clearTokens() }
        verify(exactly = 0) { sessionManager.onLoginSuccess(any()) }
    }
}
