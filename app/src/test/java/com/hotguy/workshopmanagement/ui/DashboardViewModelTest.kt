package com.hotguy.workshopmanagement.ui

import app.cash.turbine.test
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.SummaryReport
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.auth.LogoutUseCase
import com.hotguy.workshopmanagement.domain.usecase.report.GetSummaryReportUseCase
import com.hotguy.workshopmanagement.ui.dashboard.DashboardUiState
import com.hotguy.workshopmanagement.ui.dashboard.DashboardViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [DashboardViewModel].
 *
 * Se prueban los tres estados posibles (Loading, Success, Error) y la
 * acción de logout. El [SessionManager] se mockea para devolver el rol
 * deseado en cada test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getSummaryReportUseCase: GetSummaryReportUseCase = mockk()
    private val logoutUseCase:           LogoutUseCase           = mockk()
    private val sessionManager:          SessionManager          = mockk()

    private val sampleReport = SummaryReport(
        totalClients   = 10L,
        totalMechanics = 3L,
        totalVehicles  = 15L,
        totalTasks     = 25L,
        pendingTasks   = 8L,
        totalRevenue   = 4500.0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Por defecto el SessionManager tiene rol ADMIN
        every { sessionManager.currentRole } returns UserRole.ADMIN
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Estado Loading → Success ──────────────────────────────────────────────

    @Test
    fun `when init, then first emits Loading then Success with report`() = runTest {
        // Arrange
        coEvery { getSummaryReportUseCase() } returns Result.success(sampleReport)

        // Act — crear el ViewModel lanza loadReport() en init{}
        val viewModel = DashboardViewModel(getSummaryReportUseCase, logoutUseCase, sessionManager)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue("El primer estado debe ser Loading", loading is DashboardUiState.Loading)

            advanceUntilIdle()

            val success = awaitItem()
            assertTrue("El segundo estado debe ser Success", success is DashboardUiState.Success)

            val data = success as DashboardUiState.Success
            assertEquals(sampleReport, data.report)
            assertEquals(UserRole.ADMIN, data.role)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Estado Loading → Error ────────────────────────────────────────────────

    @Test
    fun `when report fails, then emits Loading then Error`() = runTest {
        coEvery { getSummaryReportUseCase() } returns
            Result.failure(Exception("Error de red"))

        val viewModel = DashboardViewModel(getSummaryReportUseCase, logoutUseCase, sessionManager)

        viewModel.uiState.test {
            awaitItem() // Loading

            advanceUntilIdle()

            val error = awaitItem()
            assertTrue(error is DashboardUiState.Error)
            assertTrue((error as DashboardUiState.Error).message.contains("Error de red"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Rol en el estado ──────────────────────────────────────────────────────

    @Test
    fun `when user is MECHANIC, then Success state contains MECHANIC role`() = runTest {
        every { sessionManager.currentRole } returns UserRole.MECHANIC
        coEvery { getSummaryReportUseCase() } returns Result.success(sampleReport)

        val viewModel = DashboardViewModel(getSummaryReportUseCase, logoutUseCase, sessionManager)

        viewModel.uiState.test {
            awaitItem() // Loading
            advanceUntilIdle()

            val success = awaitItem() as DashboardUiState.Success
            assertEquals(UserRole.MECHANIC, success.role)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Retry ─────────────────────────────────────────────────────────────────

    @Test
    fun `when loadReport called after error, then retries and shows Success`() = runTest {
        // Primera llamada falla, segunda tiene éxito
        coEvery { getSummaryReportUseCase() }
            .returnsMany(
                Result.failure(Exception("Timeout")),
                Result.success(sampleReport)
            )

        val viewModel = DashboardViewModel(getSummaryReportUseCase, logoutUseCase, sessionManager)
        advanceUntilIdle() // primera carga → error

        viewModel.uiState.test {
            awaitItem() // estado de error actual

            // Reintentar
            viewModel.loadReport()
            advanceUntilIdle()

            val loading = awaitItem()
            assertTrue(loading is DashboardUiState.Loading)

            val success = awaitItem()
            assertTrue(success is DashboardUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    fun `when onLogout called, then invokes logoutUseCase`() = runTest {
        coEvery { getSummaryReportUseCase() } returns Result.success(sampleReport)
        coEvery { logoutUseCase() } returns Result.success(Unit)

        val viewModel = DashboardViewModel(getSummaryReportUseCase, logoutUseCase, sessionManager)
        advanceUntilIdle()

        viewModel.onLogout()
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 1) { logoutUseCase() }
    }
}
