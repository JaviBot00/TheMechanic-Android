package com.hotguy.workshopmanagement.ui.client.list

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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.hotguy.workshopmanagement.domain.model.Client
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/**
 * Pantalla de lista de clientes con búsqueda por apellido y paginación infinita.
 * El botón de crear solo es visible para ADMIN y MECHANIC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    onClientClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    onBackClick:   () -> Unit,
    viewModel:     ClientListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clients = viewModel.clients.collectAsLazyPagingItems()
    val canCreate = uiState.userRole != UserRole.CLIENT

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(Icons.Outlined.Add, "Nuevo cliente")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Barra de búsqueda
            OutlinedTextField(
                value         = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label         = { Text("Buscar por apellido") },
                leadingIcon   = { Icon(Icons.Outlined.Search, null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ClientPagingList(clients = clients, onClientClick = onClientClick)
        }
    }
}

@Composable
private fun ClientPagingList(clients: LazyPagingItems<Client>, onClientClick: (Long) -> Unit) {
    when (clients.loadState.refresh) {
        is LoadState.Loading -> LoadingBox()
        is LoadState.Error   -> ErrorBox(
            message = (clients.loadState.refresh as LoadState.Error).error.message ?: "Error",
            onRetry = { clients.retry() }
        )
        else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(clients.itemCount) { index ->
                clients[index]?.let { client ->
                    ClientItem(client = client, onClick = { onClientClick(client.id) })
                }
            }
            // Indicador de carga al final de la lista (carga de página siguiente)
            if (clients.loadState.append is LoadState.Loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientItem(client: Client, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(client.fullName, style = MaterialTheme.typography.titleMedium)
            Text("NIF: ${client.nif}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${client.vehicleCount} vehículo(s)", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
