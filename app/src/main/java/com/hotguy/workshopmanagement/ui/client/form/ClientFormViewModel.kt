package com.hotguy.workshopmanagement.ui.client.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.domain.usecase.client.CreateClientUseCase
import com.hotguy.workshopmanagement.domain.usecase.client.GetClientByIdUseCase
import com.hotguy.workshopmanagement.domain.usecase.client.UpdateClientUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientFormUiState(
    val clientCode: String  = "",
    val name:       String  = "",
    val surname1:   String  = "",
    val surname2:   String  = "",
    val nif:        String  = "",
    val email:      String  = "",
    val telephone:  String  = "",
    val isLoading:  Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ClientFormEvent { data object SavedSuccessfully : ClientFormEvent }

/**
 * ViewModel del formulario de cliente (crear y editar).
 * Si [clientId] no es null, carga los datos actuales del cliente para pre-rellenar
 * el formulario en modo edición.
 */
@HiltViewModel
class ClientFormViewModel @Inject constructor(
    savedStateHandle:       SavedStateHandle,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val createClientUseCase:  CreateClientUseCase,
    private val updateClientUseCase:  UpdateClientUseCase
) : ViewModel() {

    private val clientId: Long? = savedStateHandle.toRoute<AppRoutes.ClientForm>().clientId

    private val _uiState = MutableStateFlow(ClientFormUiState(isEditMode = clientId != null))
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ClientFormEvent>()
    val events = _events.asSharedFlow()

    init { if (clientId != null) loadClient(clientId) }

    private fun loadClient(id: Long) {
        viewModelScope.launch {
            getClientByIdUseCase(id).onSuccess { c ->
                _uiState.update { it.copy(
                    clientCode = c.clientCode.toString(), name = c.name,
                    surname1 = c.surname1, surname2 = c.surname2 ?: "",
                    nif = c.nif, email = c.email, telephone = c.telephone ?: ""
                )}
            }
        }
    }

    fun onClientCodeChange(v: String) { _uiState.update { it.copy(clientCode = v) } }
    fun onNameChange(v: String)       { _uiState.update { it.copy(name = v) } }
    fun onSurname1Change(v: String)   { _uiState.update { it.copy(surname1 = v) } }
    fun onSurname2Change(v: String)   { _uiState.update { it.copy(surname2 = v) } }
    fun onNifChange(v: String)        { _uiState.update { it.copy(nif = v) } }
    fun onEmailChange(v: String)      { _uiState.update { it.copy(email = v) } }
    fun onTelephoneChange(v: String)  { _uiState.update { it.copy(telephone = v) } }

    fun onSaveClick() {
        val s = _uiState.value
        val code = s.clientCode.toIntOrNull() ?: run {
            _uiState.update { it.copy(errorMessage = "El código debe ser un número") }; return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (clientId != null) {
                updateClientUseCase(clientId, code, s.name, s.surname1, s.surname2.ifBlank { null },
                    s.nif, s.email, s.telephone.ifBlank { null })
            } else {
                createClientUseCase(code, s.name, s.surname1, s.surname2.ifBlank { null },
                    s.nif, s.email, s.telephone.ifBlank { null })
            }
            result.onSuccess { _events.emit(ClientFormEvent.SavedSuccessfully) }
                  .onFailure { _uiState.update { it.copy(errorMessage = it.errorMessage ?: "Error al guardar") } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
