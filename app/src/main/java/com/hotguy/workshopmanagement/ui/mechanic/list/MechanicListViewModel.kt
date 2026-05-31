package com.hotguy.workshopmanagement.ui.mechanic.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.Mechanic
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.mechanic.GetMechanicsUseCase
import com.hotguy.workshopmanagement.domain.usecase.mechanic.SearchMechanicsBySpecialtyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MechanicListUiState(val query: String = "", val userRole: UserRole = UserRole.MECHANIC)

@HiltViewModel
class MechanicListViewModel @Inject constructor(
    private val getMechanicsUseCase:             GetMechanicsUseCase,
    private val searchMechanicsBySpecialtyUseCase: SearchMechanicsBySpecialtyUseCase,
    private val sessionManager:                  SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MechanicListUiState(userRole = sessionManager.currentRole ?: UserRole.MECHANIC))
    val uiState = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val mechanics: Flow<PagingData<Mechanic>> = _uiState
        .flatMapLatest { state ->
            if (state.query.isBlank()) getMechanicsUseCase()
            else searchMechanicsBySpecialtyUseCase(state.query)
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) { _uiState.update { it.copy(query = query) } }
}
