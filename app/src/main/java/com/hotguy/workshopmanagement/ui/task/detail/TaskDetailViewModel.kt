package com.hotguy.workshopmanagement.ui.task.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import com.hotguy.workshopmanagement.domain.usecase.task.*
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TaskDetailUiState {
    data object Loading : TaskDetailUiState()
    data class Success(val task: WorkshopTask, val role: UserRole) : TaskDetailUiState()
    data class Error(val message: String) : TaskDetailUiState()
}

sealed interface TaskDetailEvent {
    data object DeletedSuccessfully : TaskDetailEvent
    data class ActionError(val message: String) : TaskDetailEvent
}

/**
 * ViewModel del detalle de tarea.
 * Concentra todas las acciones del ciclo de vida de la tarea:
 * añadir horas, finalizar, marcar como pagada y eliminar.
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle:       SavedStateHandle,
    private val getTaskByIdUseCase:      GetTaskByIdUseCase,
    private val addHoursToTaskUseCase:   AddHoursToTaskUseCase,
    private val finishTaskUseCase:       FinishTaskUseCase,
    private val markTaskAsPaidUseCase:   MarkTaskAsPaidUseCase,
    private val deleteTaskUseCase:       DeleteTaskUseCase,
    private val sessionManager:          SessionManager
) : ViewModel() {

    private val taskId = savedStateHandle.toRoute<AppRoutes.TaskDetail>().taskId
    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<TaskDetailEvent>()
    val events = _events.asSharedFlow()

    init { loadTask() }

    fun loadTask() {
        _uiState.update { TaskDetailUiState.Loading }
        viewModelScope.launch {
            val role = sessionManager.currentRole ?: UserRole.MECHANIC
            getTaskByIdUseCase(taskId)
                .onSuccess { report -> _uiState.update { TaskDetailUiState.Success(report, role) } }
                .onFailure { e -> _uiState.update { TaskDetailUiState.Error(e.message ?: "Error") } }
        }
    }

    fun onAddHours(hours: Float) {
        viewModelScope.launch {
            addHoursToTaskUseCase(taskId, hours)
                .onSuccess { loadTask() }
                .onFailure { _events.emit(TaskDetailEvent.ActionError(it.message ?: "Error")) }
        }
    }

    fun onFinishTask(solution: String?) {
        viewModelScope.launch {
            finishTaskUseCase(taskId, solution)
                .onSuccess { loadTask() }
                .onFailure { _events.emit(TaskDetailEvent.ActionError(it.message ?: "Error")) }
        }
    }

    fun onMarkAsPaid() {
        viewModelScope.launch {
            markTaskAsPaidUseCase(taskId)
                .onSuccess { loadTask() }
                .onFailure { _events.emit(TaskDetailEvent.ActionError(it.message ?: "Error")) }
        }
    }

    fun onDeleteTask() {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
                .onSuccess { _events.emit(TaskDetailEvent.DeletedSuccessfully) }
                .onFailure { _events.emit(TaskDetailEvent.ActionError(it.message ?: "Error")) }
        }
    }
}
