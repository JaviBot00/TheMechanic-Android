package com.hotguy.workshopmanagement.ui.task.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hotguy.workshopmanagement.domain.model.TaskStatus
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import com.hotguy.workshopmanagement.domain.repository.TaskFilter
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox
import com.hotguy.workshopmanagement.ui.theme.StatusFinishedColor
import com.hotguy.workshopmanagement.ui.theme.StatusInProgressColor
import com.hotguy.workshopmanagement.ui.theme.StatusPaidColor
import com.hotguy.workshopmanagement.ui.theme.StatusPendingColor

/**
 * Pantalla de lista de tareas con chips de filtro (Todas / Pendientes / Sin pagar).
 * Los filtros cambian el endpoint llamado, no filtran en cliente.
 * El botón de crear es visible para ADMIN y MECHANIC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    filter: String, onTaskClick: (Long) -> Unit,
    onCreateClick: () -> Unit, onBackClick: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(filter) { viewModel.init(filter) }

    val tasks = remember(uiState.activeFilter) {
        viewModel.getTasks(uiState.activeFilter)
    }.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            if (uiState.userRole != UserRole.CLIENT) {
                FloatingActionButton(onClick = onCreateClick) { Icon(Icons.Outlined.Add, "Nueva tarea") }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Chips de filtro
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    TaskFilter.ALL     to "Todas",
                    TaskFilter.PENDING to "Pendientes",
                    TaskFilter.UNPAID  to "Sin pagar"
                ).forEach { (f, label) ->
                    FilterChip(
                        selected = uiState.activeFilter == f,
                        onClick  = { viewModel.setFilter(f) },
                        label    = { Text(label) }
                    )
                }
            }

            when (tasks.loadState.refresh) {
                is LoadState.Loading -> LoadingBox()
                is LoadState.Error   -> ErrorBox(
                    (tasks.loadState.refresh as LoadState.Error).error.message ?: "Error",
                    { tasks.retry() }
                )
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasks.itemCount) { i ->
                        tasks[i]?.let { TaskItem(it) { onTaskClick(it.id) } }
                    }
                    if (tasks.loadState.append is LoadState.Loading) {
                        item { Box(Modifier.fillMaxWidth().padding(8.dp), Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp)) } }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskItem(task: WorkshopTask, onClick: () -> Unit) {
    // Color de fondo del chip según el estado
    val chipColor = when (task.status) {
        TaskStatus.PENDING     -> StatusPendingColor
        TaskStatus.IN_PROGRESS -> StatusInProgressColor
        TaskStatus.FINISHED    -> StatusFinishedColor
        TaskStatus.PAID        -> StatusPaidColor
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(task.vehicleReg, style = MaterialTheme.typography.titleMedium)
                Surface(color = chipColor, shape = MaterialTheme.shapes.small) {
                    Text(task.status.label, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Text(task.diagnostic, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Text("Mecánico: ${task.mechanicName}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Progreso: %.0f%%  ·  Coste estimado: %.2f €".format(task.progress, task.estimatedCost),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
