package com.hotguy.workshopmanagement.ui.mechanic.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hotguy.workshopmanagement.domain.model.Mechanic
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/** Lista paginada de mecánicos con búsqueda por especialidad. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicListScreen(
    onMechanicClick: (Long) -> Unit, onCreateClick: () -> Unit, onBackClick: () -> Unit,
    viewModel: MechanicListViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val mechanics = viewModel.mechanics.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mecánicos") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            // Solo ADMIN puede crear mecánicos
            if (uiState.userRole == UserRole.ADMIN) {
                FloatingActionButton(onClick = onCreateClick) { Icon(Icons.Outlined.Add, "Nuevo mecánico") }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.query, onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar por especialidad") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            when (mechanics.loadState.refresh) {
                is LoadState.Loading -> LoadingBox()
                is LoadState.Error   -> ErrorBox((mechanics.loadState.refresh as LoadState.Error).error.message ?: "Error", { mechanics.retry() })
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(mechanics.itemCount) { i ->
                        mechanics[i]?.let { m ->
                            Card(onClick = { onMechanicClick(m.id) }, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(m.fullName, style = MaterialTheme.typography.titleMedium)
                                    Text("Especialidad: ${m.specialty}", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Tareas asignadas: ${m.taskCount}", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (mechanics.loadState.append is LoadState.Loading) {
                        item { Box(Modifier.fillMaxWidth().padding(8.dp), Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp)) } }
                    }
                }
            }
        }
    }
}
