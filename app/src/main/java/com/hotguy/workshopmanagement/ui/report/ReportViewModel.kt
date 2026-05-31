package com.hotguy.workshopmanagement.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotguy.workshopmanagement.domain.model.SummaryReport
import com.hotguy.workshopmanagement.domain.usecase.report.GetSummaryReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReportUiState {
    data object Loading : ReportUiState()
    data class Success(val report: SummaryReport) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

/**
 * ViewModel de la pantalla de reporte estadístico.
 * Solo accesible para ADMIN y MECHANIC (el NavGraph no registra esta ruta
 * como destino alcanzable para CLIENT).
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getSummaryReportUseCase: GetSummaryReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init { loadReport() }

    fun loadReport() {
        _uiState.update { ReportUiState.Loading }
        viewModelScope.launch {
            getSummaryReportUseCase()
                .onSuccess { _uiState.update { ReportUiState.Success(it) } }
                .onFailure { _uiState.update { ReportUiState.Error(it.message ?: "Error al cargar el reporte") } }
        }
    }
}
