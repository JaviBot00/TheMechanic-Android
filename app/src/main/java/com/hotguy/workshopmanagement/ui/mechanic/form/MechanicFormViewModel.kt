package com.hotguy.workshopmanagement.ui.mechanic.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.domain.usecase.mechanic.CreateMechanicUseCase
import com.hotguy.workshopmanagement.domain.usecase.mechanic.GetMechanicByIdUseCase
import com.hotguy.workshopmanagement.domain.usecase.mechanic.UpdateMechanicUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MechanicFormUiState(
    val name: String = "", val surname1: String = "", val surname2: String = "",
    val nif: String = "", val email: String = "", val telephone: String = "",
    val registrationDate: String = LocalDate.now().toString(),
    val specialty: String = "",
    val isLoading: Boolean = false, val isEditMode: Boolean = false,
    val errorMessage: String? = null
)
sealed interface MechanicFormEvent { data object SavedSuccessfully : MechanicFormEvent }

@HiltViewModel
class MechanicFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMechanicByIdUseCase: GetMechanicByIdUseCase,
    private val createMechanicUseCase:  CreateMechanicUseCase,
    private val updateMechanicUseCase:  UpdateMechanicUseCase
) : ViewModel() {

    private val mechanicId = savedStateHandle.toRoute<AppRoutes.MechanicForm>().mechanicId
    private val _uiState = MutableStateFlow(MechanicFormUiState(isEditMode = mechanicId != null))
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<MechanicFormEvent>()
    val events = _events.asSharedFlow()

    init { if (mechanicId != null) loadMechanic(mechanicId) }

    private fun loadMechanic(id: Long) {
        viewModelScope.launch {
            getMechanicByIdUseCase(id).onSuccess { m ->
                _uiState.update { it.copy(
                    name = m.name, surname1 = m.surname1, surname2 = m.surname2 ?: "",
                    nif = m.nif, email = m.email, telephone = m.telephone ?: "",
                    registrationDate = m.registrationDate.toString(), specialty = m.specialty
                )}
            }
        }
    }

    fun onNameChange(v: String)             { _uiState.update { it.copy(name = v) } }
    fun onSurname1Change(v: String)         { _uiState.update { it.copy(surname1 = v) } }
    fun onSurname2Change(v: String)         { _uiState.update { it.copy(surname2 = v) } }
    fun onNifChange(v: String)              { _uiState.update { it.copy(nif = v) } }
    fun onEmailChange(v: String)            { _uiState.update { it.copy(email = v) } }
    fun onTelephoneChange(v: String)        { _uiState.update { it.copy(telephone = v) } }
    fun onRegistrationDateChange(v: String) { _uiState.update { it.copy(registrationDate = v) } }
    fun onSpecialtyChange(v: String)        { _uiState.update { it.copy(specialty = v) } }

    fun onSaveClick() {
        val s = _uiState.value
        val date = runCatching { LocalDate.parse(s.registrationDate) }.getOrElse {
            _uiState.update { it.copy(errorMessage = "Fecha inválida. Formato: yyyy-MM-dd") }; return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (mechanicId != null)
                updateMechanicUseCase(mechanicId, s.name, s.surname1, s.surname2.ifBlank { null },
                    s.nif, s.email, s.telephone.ifBlank { null }, date, s.specialty)
            else
                createMechanicUseCase(s.name, s.surname1, s.surname2.ifBlank { null },
                    s.nif, s.email, s.telephone.ifBlank { null }, date, s.specialty)
            result.onSuccess { _events.emit(MechanicFormEvent.SavedSuccessfully) }
                  .onFailure { _uiState.update { it.copy(errorMessage = it.message ?: "Error") } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
