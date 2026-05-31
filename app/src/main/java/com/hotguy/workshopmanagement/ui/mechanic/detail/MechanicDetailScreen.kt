package com.hotguy.workshopmanagement.ui.mechanic.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.Mechanic
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/** Pantalla de detalle de mecánico. Solo ADMIN puede editar o eliminar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicDetailScreen(
    mechanicId: Long, onEditClick: () -> Unit,
    onTaskClick: (Long) -> Unit, onBackClick: () -> Unit,
    viewModel: MechanicDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { when (it) { is MechanicDetailEvent.DeletedSuccessfully -> onBackClick() } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del mecánico") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } },
                actions = {
                    val s = uiState
                    if (s is MechanicDetailUiState.Success && s.role == UserRole.ADMIN) {
                        IconButton(onClick = onEditClick) { Icon(Icons.Outlined.Edit, "Editar") }
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Outlined.Delete, "Eliminar") }
                    }
                }
            )
        }
    ) { padding ->
        when (val s = uiState) {
            is MechanicDetailUiState.Loading -> LoadingBox(Modifier.padding(padding))
            is MechanicDetailUiState.Error   -> ErrorBox(s.message, viewModel::loadMechanic, Modifier.padding(padding))
            is MechanicDetailUiState.Success -> MechanicInfo(s.mechanic, Modifier.padding(padding))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Eliminar mecánico") },
            text    = { Text("¿Eliminar este mecánico? Solo es posible si no tiene tareas activas asignadas.") },
            confirmButton = { TextButton(onClick = { showDeleteDialog = false; viewModel.onDeleteClick() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun MechanicInfo(m: Mechanic, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow("Nombre completo",  m.fullName)
        InfoRow("NIF",              m.nif)
        InfoRow("Email",            m.email)
        m.telephone?.let { InfoRow("Teléfono", it) }
        InfoRow("Especialidad",     m.specialty)
        InfoRow("Fecha de alta",    m.registrationDate.toString())
        InfoRow("Tareas asignadas", m.taskCount.toString())
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
