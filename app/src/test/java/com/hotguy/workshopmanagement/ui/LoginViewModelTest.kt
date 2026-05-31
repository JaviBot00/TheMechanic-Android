package com.hotguy.workshopmanagement.ui

import app.cash.turbine.test
import com.hotguy.workshopmanagement.domain.model.AuthToken
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.auth.LoginUseCase
import com.hotguy.workshopmanagement.ui.auth.LoginEvent
import com.hotguy.workshopmanagement.ui.auth.LoginViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [LoginViewModel].
 *
 * Usamos Turbine para testear los [kotlinx.coroutines.flow.Flow] de forma
 * legible y determinista: `uiState.test { ... }` suscribe el Flow en el
 * test y permite leer los valores emitidos de uno en uno.
 *
 * Usamos [StandardTestDispatcher] para controlar cuándo se ejecutan las
 * coroutinas: `advanceUntilIdle()` ejecuta todas las coroutinas pendientes
 * hasta que no queda ninguna por ejecutar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // ── Setup ─────────────────────────────────────────────────────────────────

    private val testDispatcher = StandardTestDispatcher()
    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        // Reemplazar el dispatcher Main por uno controlable en tests
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Estado inicial ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has empty fields and no error`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.username)
            assertEquals("", state.password)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Actualizaciones de campos ─────────────────────────────────────────────

    @Test
    fun `onUsernameChange updates username in state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.onUsernameChange("admin")
            val state = awaitItem()

            assertEquals("admin", state.username)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPasswordChange updates password in state`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onPasswordChange("secret123")
            val state = awaitItem()

            assertEquals("secret123", state.password)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTogglePasswordVisibility toggles passwordVisible flag`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.passwordVisible)

            viewModel.onTogglePasswordVisibility()
            val toggled = awaitItem()
            assertTrue(toggled.passwordVisible)

            viewModel.onTogglePasswordVisibility()
            val toggledBack = awaitItem()
            assertFalse(toggledBack.passwordVisible)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onUsernameChange clears previous error message`() = runTest {
        // Arrange — simular que hay un error previo
        coEvery { loginUseCase(any(), any()) } returns
            Result.failure(Exception("HTTP 401 Unauthorized"))
        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("wrong")
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Act — el usuario corrige el username
        viewModel.onUsernameChange("admin_corrected")

        // Assert — el error debe haberse limpiado
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Login exitoso ─────────────────────────────────────────────────────────

    @Test
    fun `given valid credentials, when onLoginClick, then emits LoginSuccess event`() = runTest {
        // Arrange
        val token = AuthToken("access", "refresh", UserRole.ADMIN)
        coEvery { loginUseCase("admin", "secret") } returns Result.success(token)

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("secret")

        // Act + Assert — testear el SharedFlow de eventos con Turbine
        viewModel.events.test {
            viewModel.onLoginClick()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is LoginEvent.LoginSuccess)
            assertEquals(UserRole.ADMIN, (event as LoginEvent.LoginSuccess).role)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given valid credentials, when onLoginClick, then loading is true then false`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns
            Result.success(AuthToken("access", "refresh", UserRole.MECHANIC))

        viewModel.onUsernameChange("mec")
        viewModel.onPasswordChange("pass")

        viewModel.uiState.test {
            awaitItem() // estado inicial (username vacío)
            awaitItem() // después de onUsernameChange
            awaitItem() // después de onPasswordChange

            viewModel.onLoginClick()

            val loadingState = awaitItem()
            assertTrue("Debe estar cargando", loadingState.isLoading)

            advanceUntilIdle()

            val finalState = awaitItem()
            assertFalse("Debe haber terminado de cargar", finalState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Login fallido ─────────────────────────────────────────────────────────

    @Test
    fun `given wrong credentials, when onLoginClick, then shows error message`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns
            Result.failure(Exception("HTTP 401 Unauthorized"))

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("wrongpass")
        viewModel.onLoginClick()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.errorMessage)
            assertTrue(state.errorMessage!!.contains("Credenciales incorrectas"))
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given network error, when onLoginClick, then shows connectivity error message`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns
            Result.failure(Exception("Unable to resolve host"))

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("pass")
        viewModel.onLoginClick()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.errorMessage)
            assertTrue(state.errorMessage!!.contains("servidor"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given duplicate click while loading, when onLoginClick twice, then only one repository call`() = runTest {
        // Arrange — la primera llamada tarda (simula latencia de red)
        coEvery { loginUseCase(any(), any()) } returns
            Result.success(AuthToken("t", "r", UserRole.CLIENT))

        viewModel.onUsernameChange("user")
        viewModel.onPasswordChange("pass")

        // Act — dos clics rápidos
        viewModel.onLoginClick()
        viewModel.onLoginClick()  // debe ignorarse porque isLoading = true
        advanceUntilIdle()

        // Assert — el use case solo se llamó una vez
        io.mockk.coVerify(exactly = 1) { loginUseCase(any(), any()) }
    }
}
