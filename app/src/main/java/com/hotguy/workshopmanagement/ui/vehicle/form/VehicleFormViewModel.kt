package com.hotguy.workshopmanagement.ui.vehicle.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.domain.model.VehicleType
import com.hotguy.workshopmanagement.domain.usecase.vehicle.CreateVehicleUseCase
import com.hotguy.workshopmanagement.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.hotguy.workshopmanagement.domain.usecase.vehicle.UpdateVehicleUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleFormUiState(
    val registrationCode: String      = "",
    val model:            String      = "",
    val type:             VehicleType = VehicleType.CAR,
    val clientId:         String      = "",
    val isLoading:        Boolean     = false,
    val isEditMode:       Boolean     = false,
    val errorMessage:     String?     = null
)

sealed interface VehicleFormEvent { data object SavedSuccessfully : VehicleFormEvent }

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val createVehicleUseCase:  CreateVehicleUseCase,
    private val updateVehicleUseCase:  UpdateVehicleUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AppRoutes.VehicleForm>()
    private val vehicleId = route.vehicleId
    private val preselectedClientId = route.clientId

    private val _uiState = MutableStateFlow(
        VehicleFormUiState(
            isEditMode = vehicleId != null,
            clientId   = preselectedClientId?.takeIf { it > 0 }?.toString() ?: ""
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VehicleFormEvent>()
    val events = _events.asSharedFlow()

    init { if (vehicleId != null) loadVehicle(vehicleId) }

    private fun loadVehicle(id: Long) {
        viewModelScope.launch {
            getVehicleByIdUseCase(id).onSuccess { v ->
                _uiState.update { it.copy(
                    registrationCode = v.registrationCode, model = v.model,
                    type = v.type, clientId = v.clientId.toString()
                )}
            }
        }
    }

    fun onRegistrationCodeChange(v: String) { _uiState.update { it.copy(registrationCode = v) } }
    fun onModelChange(v: String)            { _uiState.update { it.copy(model = v) } }
    fun onTypeChange(v: VehicleType)        { _uiState.update { it.copy(type = v) } }
    fun onClientIdChange(v: String)         { _uiState.update { it.copy(clientId = v) } }

    fun onSaveClick() {
        val s = _uiState.value
        val cid = s.clientId.toLongOrNull() ?: run {
            _uiState.update { it.copy(errorMessage = "ID de cliente inválido") }; return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (vehicleId != null)
                updateVehicleUseCase(vehicleId, s.registrationCode, s.model, s.type, cid)
            else
                createVehicleUseCase(s.registrationCode, s.model, s.type, cid)
            result.onSuccess { _events.emit(VehicleFormEvent.SavedSuccessfully) }
                  .onFailure { _uiState.update { it.copy(errorMessage = it.errorMessage ?: "Error al guardar") } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
