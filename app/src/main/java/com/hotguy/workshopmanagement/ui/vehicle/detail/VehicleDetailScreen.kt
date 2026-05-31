package com.hotguy.workshopmanagement.ui.vehicle.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/**
 * Pantalla de detalle de un vehículo.
 * Muestra información del vehículo, tarifas y accesos a sus tareas.
 * Las acciones de editar/eliminar se controlan por rol.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Long, onEditClick: () -> Unit,
    onTaskClick: (Long) -> Unit, onCreateTask: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) { is VehicleDetailEvent.DeletedSuccessfully -> onBackClick() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del vehículo") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } },
                actions = {
                    val s = uiState
                    if (s is VehicleDetailUiState.Success) {
                        if (s.role != UserRole.CLIENT) {
                            IconButton(onClick = onEditClick) { Icon(Icons.Outlined.Edit, "Editar") }
                        }
                        if (s.role == UserRole.ADMIN) {
                            IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Outlined.Delete, "Eliminar") }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val s = uiState
            if (s is VehicleDetailUiState.Success && s.role != UserRole.CLIENT) {
                FloatingActionButton(onClick = onCreateTask) {
                    Icon(Icons.Outlined.Add, "Nueva tarea")
                }
            }
        }
    ) { padding ->
        when (val s = uiState) {
            is VehicleDetailUiState.Loading -> LoadingBox(Modifier.padding(padding))
            is VehicleDetailUiState.Error   -> ErrorBox(s.message, viewModel::loadVehicle, Modifier.padding(padding))
            is VehicleDetailUiState.Success -> VehicleInfo(s.vehicle, Modifier.padding(padding))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Eliminar vehículo") },
            text    = { Text("¿Eliminar este vehículo? Solo es posible si no tiene tareas activas.") },
            confirmButton = { TextButton(onClick = { showDeleteDialog = false; viewModel.onDeleteClick() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun VehicleInfo(vehicle: Vehicle, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow("Matrícula",        vehicle.registrationCode)
        InfoRow("Modelo",           vehicle.model)
        InfoRow("Tipo",             vehicle.type.labelEs)
        InfoRow("Tarifa horaria",   "${vehicle.type.hourlyRate} €/h")
        InfoRow("Cargo fijo",       "${vehicle.type.fixedFee} €")
        InfoRow("Propietario",      vehicle.clientName)
        InfoRow("Tareas totales",   vehicle.taskCount.toString())
        InfoRow("Completadas",      "%.0f%%".format(vehicle.completionPct))
        InfoRow("Facturación total","%.2f €".format(vehicle.totalRevenue))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}
