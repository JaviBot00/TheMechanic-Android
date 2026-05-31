package com.hotguy.workshopmanagement.ui.client.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.Client
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.client.DeleteClientUseCase
import com.hotguy.workshopmanagement.domain.usecase.client.GetClientByIdUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientDetailUiState {
    data object Loading : ClientDetailUiState()
    data class Success(val client: Client, val role: UserRole) : ClientDetailUiState()
    data class Error(val message: String) : ClientDetailUiState()
}

sealed interface ClientDetailEvent {
    data object DeletedSuccessfully : ClientDetailEvent
}

/**
 * ViewModel del detalle de un cliente.
 * Lee el clientId de [SavedStateHandle] (inyectado por Navigation Compose)
 * para no depender de parámetros en el constructor.
 */
@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    savedStateHandle:    SavedStateHandle,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val deleteClientUseCase:  DeleteClientUseCase,
    private val sessionManager:       SessionManager
) : ViewModel() {

    private val clientId: Long = savedStateHandle.toRoute<AppRoutes.ClientDetail>().clientId

    private val _uiState = MutableStateFlow<ClientDetailUiState>(ClientDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ClientDetailEvent>()
    val events = _events.asSharedFlow()

    init { loadClient() }

    fun loadClient() {
        _uiState.update { ClientDetailUiState.Loading }
        viewModelScope.launch {
            val role = sessionManager.currentRole ?: UserRole.MECHANIC
            getClientByIdUseCase(clientId)
                .onSuccess { report -> _uiState.update { ClientDetailUiState.Success(report, role) } }
                .onFailure { e -> _uiState.update { ClientDetailUiState.Error(e.message ?: "Error") } }
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            deleteClientUseCase(clientId)
                .onSuccess { _events.emit(ClientDetailEvent.DeletedSuccessfully) }
                .onFailure { e -> _uiState.update { ClientDetailUiState.Error(e.message ?: "Error al eliminar") } }
        }
    }
}
