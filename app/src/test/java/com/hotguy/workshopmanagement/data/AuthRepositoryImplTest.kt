package com.hotguy.workshopmanagement.data

import com.hotguy.workshopmanagement.data.local.TokenDataStore
import com.hotguy.workshopmanagement.data.remote.api.AuthApiService
import com.hotguy.workshopmanagement.data.remote.dto.auth.AuthResponseDto
import com.hotguy.workshopmanagement.data.repository.AuthRepositoryImpl
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [AuthRepositoryImpl].
 *
 * Se prueba la orquestación entre el servicio API, el DataStore y el
 * SessionManager durante el login y el logout. Todos los colaboradores
 * son mocks de MockK.
 */
class AuthRepositoryImplTest {

    private val authApiService: AuthApiService = mockk()
    private val tokenDataStore: TokenDataStore = mockk()
    private val sessionManager: SessionManager = mockk(relaxed = true)

    private lateinit var repository: AuthRepositoryImpl

    private val authResponseDto = AuthResponseDto(
        accessToken  = "access.token.jwt",
        refreshToken = "refresh-uuid-1234",
        tokenType    = "Bearer",
        role         = "ROLE_ADMIN"
    )

    @Before
    fun setUp() {
        repository = AuthRepositoryImpl(authApiService, tokenDataStore, sessionManager)
    }

    // ── Login exitoso ─────────────────────────────────────────────────────────

    @Test
    fun `given valid credentials, when login, then saves tokens and notifies SessionManager`() = runTest {
        // Arrange
        coEvery { authApiService.login(any()) } returns authResponseDto
        coEvery { tokenDataStore.saveTokens(any(), any()) } returns Unit

        // Act
        val result = repository.login("admin", "secret")

        // Assert
        assertTrue(result.isSuccess)
        val token = result.getOrNull()!!
        assertEquals("access.token.jwt",  token.accessToken)
        assertEquals("refresh-uuid-1234", token.refreshToken)
        assertEquals(UserRole.ADMIN,       token.role)

        // Los tokens deben haberse guardado en disco
        coVerify(exactly = 1) { tokenDataStore.saveTokens("access.token.jwt", "refresh-uuid-1234") }

        // El SessionManager debe haberse notificado del login exitoso
        verify(exactly = 1) { sessionManager.onLoginSuccess(UserRole.ADMIN) }
    }

    @Test
    fun `given network error, when login, then returns failure without touching DataStore`() = runTest {
        // Arrange
        coEvery { authApiService.login(any()) } throws Exception("HTTP 401 Unauthorized")

        // Act
        val result = repository.login("admin", "wrongpass")

        // Assert
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { tokenDataStore.saveTokens(any(), any()) }
        verify(exactly = 0) { sessionManager.onLoginSuccess(any()) }
    }

    // ── Rol desconocido ───────────────────────────────────────────────────────

    @Test
    fun `given unknown role in response, when login, then falls back to CLIENT role`() = runTest {
        // Arrange — el backend devuelve un rol desconocido
        val unknownRoleDto = authResponseDto.copy(role = "ROLE_SUPERADMIN")
        coEvery { authApiService.login(any()) } returns unknownRoleDto
        coEvery { tokenDataStore.saveTokens(any(), any()) } returns Unit

        // Act
        val result = repository.login("admin", "secret")

        // Assert — fallback a CLIENT (el rol más restrictivo)
        assertTrue(result.isSuccess)
        assertEquals(UserRole.CLIENT, result.getOrNull()?.role)
        verify { sessionManager.onLoginSuccess(UserRole.CLIENT) }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    fun `when logout, then clears tokens and notifies SessionManager`() = runTest {
        // Arrange
        coEvery { authApiService.logout() } returns Unit
        coEvery { tokenDataStore.clearTokens() } returns Unit

        // Act
        val result = repository.logout()

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { tokenDataStore.clearTokens() }
        verify(exactly = 1) { sessionManager.onLogout() }
    }

    @Test
    fun `when logout and server fails, then still clears local tokens`() = runTest {
        // Simula que el servidor no responde (red caída)
        coEvery { authApiService.logout() } throws Exception("Connection refused")
        coEvery { tokenDataStore.clearTokens() } returns Unit

        // Act
        val result = repository.logout()

        // Assert — el logout local siempre funciona independientemente del servidor
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { tokenDataStore.clearTokens() }
        verify(exactly = 1) { sessionManager.onLogout() }
    }
}
