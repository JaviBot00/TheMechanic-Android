package com.hotguy.workshopmanagement.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.SummaryReport
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.auth.LogoutUseCase
import com.hotguy.workshopmanagement.domain.usecase.report.GetSummaryReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val report: SummaryReport, val role: UserRole) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

/**
 * ViewModel de la pantalla de dashboard.
 * Carga el resumen estadístico al inicializarse y expone el rol para que
 * la pantalla decida qué acciones mostrar.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getSummaryReportUseCase: GetSummaryReportUseCase,
    private val logoutUseCase:           LogoutUseCase,
    private val sessionManager:          SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadReport() }

    fun loadReport() {
        _uiState.update { DashboardUiState.Loading }
        viewModelScope.launch {
            val role = sessionManager.currentRole ?: UserRole.MECHANIC
            getSummaryReportUseCase()
                .onSuccess { report -> _uiState.update { DashboardUiState.Success(report, role) } }
                .onFailure { e -> _uiState.update { DashboardUiState.Error(e.message ?: "Error") } }
        }
    }

    /** Cierra sesión. SessionManager emite Unauthenticated → NavGraph navega a Login. */
    fun onLogout() { viewModelScope.launch { logoutUseCase() } }
}
