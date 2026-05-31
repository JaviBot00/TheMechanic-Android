package com.hotguy.workshopmanagement.ui.client.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.Client
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.client.GetClientsUseCase
import com.hotguy.workshopmanagement.domain.usecase.client.SearchClientsBySurnameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ClientListUiState(
    val query:    String   = "",
    val userRole: UserRole = UserRole.MECHANIC
)

/**
 * ViewModel de la lista de clientes.
 *
 * El flujo paginado se construye con [flatMapLatest]: cada vez que cambia
 * la query de búsqueda, se cancela el flujo anterior y se crea uno nuevo
 * con el nuevo término. [cachedIn] guarda el resultado en el scope del
 * ViewModel para sobrevivir a recomposiciones.
 */
@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val getClientsUseCase:           GetClientsUseCase,
    private val searchClientsBySurnameUseCase: SearchClientsBySurnameUseCase,
    private val sessionManager:              SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ClientListUiState(userRole = sessionManager.currentRole ?: UserRole.MECHANIC)
    )
    val uiState = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val clients: Flow<PagingData<Client>> = _uiState
        .flatMapLatest { state ->
            if (state.query.isBlank()) getClientsUseCase()
            else searchClientsBySurnameUseCase(state.query)
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }
}
