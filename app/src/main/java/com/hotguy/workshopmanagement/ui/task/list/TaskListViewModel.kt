package com.hotguy.workshopmanagement.ui.task.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import com.hotguy.workshopmanagement.domain.repository.TaskFilter
import com.hotguy.workshopmanagement.domain.usecase.task.GetTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TaskListUiState(
    val activeFilter: TaskFilter = TaskFilter.ALL,
    val userRole:     UserRole   = UserRole.MECHANIC
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val sessionManager:  SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TaskListUiState(userRole = sessionManager.currentRole ?: UserRole.MECHANIC)
    )
    val uiState = _uiState.asStateFlow()

    fun init(filterString: String) {
        val filter = when (filterString) {
            "PENDING" -> TaskFilter.PENDING
            "UNPAID"  -> TaskFilter.UNPAID
            else      -> TaskFilter.ALL
        }
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    // El Flow se recrea cuando cambia el filtro con un nuevo Pager
    fun getTasks(filter: TaskFilter): Flow<PagingData<WorkshopTask>> =
        getTasksUseCase(filter).cachedIn(viewModelScope)
}
