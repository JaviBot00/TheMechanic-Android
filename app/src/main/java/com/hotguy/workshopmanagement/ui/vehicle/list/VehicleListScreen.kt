package com.hotguy.workshopmanagement.ui.vehicle.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/**
 * Pantalla de lista de vehículos.
 * Reutilizable para tres contextos:
 * - Todos los vehículos (clientId = null) — ADMIN/MECHANIC
 * - Vehículos de un cliente (clientId específico) — ADMIN/MECHANIC
 * - Vehículos propios (clientId = -1L) — CLIENT
 * El botón de crear solo aparece para ADMIN y MECHANIC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    clientId: Long?, onVehicleClick: (Long) -> Unit,
    onCreateClick: () -> Unit, onBackClick: () -> Unit,
    viewModel: VehicleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val vehicles = viewModel.vehicles.collectAsLazyPagingItems()

    LaunchedEffect(clientId) { viewModel.init(clientId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehículos") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            if (uiState.userRole != UserRole.CLIENT) {
                FloatingActionButton(onClick = onCreateClick) { Icon(Icons.Outlined.Add, "Nuevo vehículo") }
            }
        }
    ) { padding ->
        when (vehicles.loadState.refresh) {
            is LoadState.Loading -> LoadingBox(Modifier.padding(padding))
            is LoadState.Error   -> ErrorBox(
                message = (vehicles.loadState.refresh as LoadState.Error).error.message ?: "Error",
                onRetry = { vehicles.retry() }, modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(vehicles.itemCount) { index ->
                    vehicles[index]?.let { VehicleItem(it) { onVehicleClick(it.id) } }
                }
                if (vehicles.loadState.append is LoadState.Loading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(8.dp), Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleItem(vehicle: Vehicle, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(vehicle.registrationCode, style = MaterialTheme.typography.titleMedium)
            Text(vehicle.model, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${vehicle.type.labelEs} · ${vehicle.type.hourlyRate}€/h",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Propietario: ${vehicle.clientName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
