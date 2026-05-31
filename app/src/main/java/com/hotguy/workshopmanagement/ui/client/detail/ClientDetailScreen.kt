package com.hotguy.workshopmanagement.ui.client.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.Client
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/**
 * Pantalla de detalle de un cliente.
 * Muestra toda la información del cliente y, según el rol, permite editar
 * (ADMIN/MECHANIC) o eliminar (solo ADMIN).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: Long, onEditClick: () -> Unit,
    onVehicleClick: (Long) -> Unit, onBackClick: () -> Unit,
    viewModel: ClientDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ClientDetailEvent.DeletedSuccessfully -> onBackClick()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del cliente") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } },
                actions = {
                    val s = uiState
                    if (s is ClientDetailUiState.Success) {
                        if (s.role != UserRole.CLIENT) {
                            IconButton(onClick = onEditClick) { Icon(Icons.Outlined.Edit, "Editar") }
                        }
                        if (s.role == UserRole.ADMIN) {
                            IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Outlined.Delete, "Eliminar") }
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val s = uiState) {
            is ClientDetailUiState.Loading -> LoadingBox(Modifier.padding(padding))
            is ClientDetailUiState.Error   -> ErrorBox(s.message, onRetry = viewModel::loadClient, modifier = Modifier.padding(padding))
            is ClientDetailUiState.Success -> ClientInfo(s.client, Modifier.padding(padding))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cliente") },
            text  = { Text("¿Estás seguro de que quieres eliminar este cliente? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.onDeleteClick() }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ClientInfo(client: Client, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow("Nombre completo", client.fullName)
        InfoRow("NIF",            client.nif)
        InfoRow("Email",          client.email)
        client.telephone?.let { InfoRow("Teléfono", it) }
        InfoRow("Código de cliente", client.clientCode.toString())
        InfoRow("Vehículos registrados", client.vehicleCount.toString())
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
