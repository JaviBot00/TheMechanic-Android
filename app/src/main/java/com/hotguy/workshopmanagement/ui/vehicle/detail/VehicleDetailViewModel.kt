package com.hotguy.workshopmanagement.ui.vehicle.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.domain.usecase.vehicle.DeleteVehicleUseCase
import com.hotguy.workshopmanagement.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VehicleDetailUiState {
    data object Loading : VehicleDetailUiState()
    data class Success(val vehicle: Vehicle, val role: UserRole) : VehicleDetailUiState()
    data class Error(val message: String) : VehicleDetailUiState()
}

sealed interface VehicleDetailEvent {
    data object DeletedSuccessfully : VehicleDetailEvent
}

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val deleteVehicleUseCase:  DeleteVehicleUseCase,
    private val sessionManager:        SessionManager
) : ViewModel() {

    private val vehicleId = savedStateHandle.toRoute<AppRoutes.VehicleDetail>().vehicleId

    private val _uiState = MutableStateFlow<VehicleDetailUiState>(VehicleDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VehicleDetailEvent>()
    val events = _events.asSharedFlow()

    init { loadVehicle() }

    fun loadVehicle() {
        _uiState.update { VehicleDetailUiState.Loading }
        viewModelScope.launch {
            val role = sessionManager.currentRole ?: UserRole.MECHANIC
            getVehicleByIdUseCase(vehicleId)
                .onSuccess { report -> _uiState.update { VehicleDetailUiState.Success(report, role) } }
                .onFailure { e -> _uiState.update { VehicleDetailUiState.Error(e.message ?: "Error") } }
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            deleteVehicleUseCase(vehicleId)
                .onSuccess { _events.emit(VehicleDetailEvent.DeletedSuccessfully) }
                .onFailure { e -> _uiState.update { VehicleDetailUiState.Error(e.message ?: "Error al eliminar") } }
        }
    }
}
