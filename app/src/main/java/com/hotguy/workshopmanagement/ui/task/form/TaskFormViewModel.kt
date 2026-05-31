package com.hotguy.workshopmanagement.ui.task.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.domain.usecase.task.CreateTaskUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TaskFormUiState(
    val vehicleId:    String  = "",
    val mechanicId:   String  = "",
    val diagnostic:   String  = "",
    val previewHours: String  = "",
    val initDate:     String  = LocalDate.now().toString(),
    val notes:        String  = "",
    val isLoading:    Boolean = false,
    val errorMessage: String? = null
)

sealed interface TaskFormEvent { data object SavedSuccessfully : TaskFormEvent }

/**
 * ViewModel del formulario de creación de tareas.
 *
 * Si se navega desde el detalle de un vehículo, [preselectedVehicleId] ya
 * viene relleno en el formulario. El usuario solo necesita elegir mecánico
 * y rellenar el diagnóstico.
 */
@HiltViewModel
class TaskFormViewModel @Inject constructor(
    savedStateHandle:      SavedStateHandle,
    private val createTaskUseCase: CreateTaskUseCase
) : ViewModel() {

    private val preselectedVehicleId =
        savedStateHandle.toRoute<AppRoutes.TaskForm>().vehicleId

    private val _uiState = MutableStateFlow(
        TaskFormUiState(
            vehicleId = preselectedVehicleId?.takeIf { it > 0 }?.toString() ?: ""
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskFormEvent>()
    val events = _events.asSharedFlow()

    fun onVehicleIdChange(v: String)    { _uiState.update { it.copy(vehicleId = v) } }
    fun onMechanicIdChange(v: String)   { _uiState.update { it.copy(mechanicId = v) } }
    fun onDiagnosticChange(v: String)   { _uiState.update { it.copy(diagnostic = v) } }
    fun onPreviewHoursChange(v: String) { _uiState.update { it.copy(previewHours = v) } }
    fun onInitDateChange(v: String)     { _uiState.update { it.copy(initDate = v) } }
    fun onNotesChange(v: String)        { _uiState.update { it.copy(notes = v) } }

    fun onSaveClick() {
        val s = _uiState.value

        val vehicleId = s.vehicleId.toLongOrNull() ?: run {
            _uiState.update { it.copy(errorMessage = "ID de vehículo inválido") }; return
        }
        val mechanicId = s.mechanicId.toLongOrNull() ?: run {
            _uiState.update { it.copy(errorMessage = "ID de mecánico inválido") }; return
        }
        val hours = s.previewHours.toFloatOrNull() ?: run {
            _uiState.update { it.copy(errorMessage = "Horas estimadas inválidas") }; return
        }
        val date = runCatching { LocalDate.parse(s.initDate) }.getOrElse {
            _uiState.update { it.copy(errorMessage = "Fecha inválida. Formato: yyyy-MM-dd") }; return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            createTaskUseCase(
                vehicleId    = vehicleId,
                mechanicId   = mechanicId,
                diagnostic   = s.diagnostic,
                previewHours = hours,
                initDate     = date,
                notes        = s.notes.ifBlank { null }
            )
                .onSuccess { _events.emit(TaskFormEvent.SavedSuccessfully) }
                .onFailure { _uiState.update { it.copy(errorMessage = it.errorMessage ?: "Error al guardar") } }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
