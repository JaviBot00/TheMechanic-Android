package com.hotguy.workshopmanagement.ui.mechanic.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.Mechanic
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.mechanic.DeleteMechanicUseCase
import com.hotguy.workshopmanagement.domain.usecase.mechanic.GetMechanicByIdUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MechanicDetailUiState {
    data object Loading : MechanicDetailUiState()
    data class Success(val mechanic: Mechanic, val role: UserRole) : MechanicDetailUiState()
    data class Error(val message: String) : MechanicDetailUiState()
}
sealed interface MechanicDetailEvent { data object DeletedSuccessfully : MechanicDetailEvent }

@HiltViewModel
class MechanicDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMechanicByIdUseCase: GetMechanicByIdUseCase,
    private val deleteMechanicUseCase:  DeleteMechanicUseCase,
    private val sessionManager:         SessionManager
) : ViewModel() {

    private val mechanicId = savedStateHandle.toRoute<AppRoutes.MechanicDetail>().mechanicId
    private val _uiState = MutableStateFlow<MechanicDetailUiState>(MechanicDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<MechanicDetailEvent>()
    val events = _events.asSharedFlow()

    init { loadMechanic() }

    fun loadMechanic() {
        _uiState.update { MechanicDetailUiState.Loading }
        viewModelScope.launch {
            val role = sessionManager.currentRole ?: UserRole.MECHANIC
            getMechanicByIdUseCase(mechanicId)
                .onSuccess { _uiState.update { MechanicDetailUiState.Success(it, role) } }
                .onFailure { _uiState.update { MechanicDetailUiState.Error(it.message ?: "Error") } }
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            deleteMechanicUseCase(mechanicId)
                .onSuccess { _events.emit(MechanicDetailEvent.DeletedSuccessfully) }
                .onFailure { _uiState.update { MechanicDetailUiState.Error(it.message ?: "Error al eliminar") } }
        }
    }
}
