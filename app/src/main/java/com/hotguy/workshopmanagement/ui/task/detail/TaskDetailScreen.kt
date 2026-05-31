package com.hotguy.workshopmanagement.ui.task.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.TaskStatus
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox
import com.hotguy.workshopmanagement.ui.theme.*

/**
 * Pantalla de detalle de tarea con todas las acciones disponibles según rol:
 * - MECHANIC / ADMIN: añadir horas, finalizar tarea
 * - ADMIN: marcar como pagada, eliminar
 * Los botones de acción se muestran solo si la acción es posible en el estado actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long, onBackClick: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddHoursDialog by remember { mutableStateOf(false) }
    var showFinishDialog   by remember { mutableStateOf(false) }
    var showDeleteDialog   by remember { mutableStateOf(false) }
    var snackbarMessage    by remember { mutableStateOf<String?>(null) }
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskDetailEvent.DeletedSuccessfully -> onBackClick()
                is TaskDetailEvent.ActionError         -> snackbarMessage = event.message
            }
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it); snackbarMessage = null }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de tarea") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } },
                actions = {
                    val s = uiState
                    if (s is TaskDetailUiState.Success && s.role == UserRole.ADMIN && !s.task.paid) {
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Outlined.Delete, "Eliminar") }
                    }
                }
            )
        }
    ) { padding ->
        when (val s = uiState) {
            is TaskDetailUiState.Loading -> LoadingBox(Modifier.padding(padding))
            is TaskDetailUiState.Error   -> ErrorBox(s.message, viewModel::loadTask, Modifier.padding(padding))
            is TaskDetailUiState.Success -> TaskContent(
                task = s.task, role = s.role,
                onAddHoursClick = { showAddHoursDialog = true },
                onFinishClick   = { showFinishDialog = true },
                onPayClick      = { viewModel.onMarkAsPaid() },
                modifier        = Modifier.padding(padding)
            )
        }
    }

    // ── Diálogo: añadir horas ────────────────────────────────────────────────
    if (showAddHoursDialog) {
        var hoursText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddHoursDialog = false },
            title   = { Text("Añadir horas") },
            text    = {
                OutlinedTextField(
                    value = hoursText, onValueChange = { hoursText = it },
                    label = { Text("Horas a añadir") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    hoursText.toFloatOrNull()?.let { viewModel.onAddHours(it) }
                    showAddHoursDialog = false
                }) { Text("Añadir") }
            },
            dismissButton = { TextButton(onClick = { showAddHoursDialog = false }) { Text("Cancelar") } }
        )
    }

    // ── Diálogo: finalizar tarea ─────────────────────────────────────────────
    if (showFinishDialog) {
        var solution by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finalizar tarea") },
            text  = {
                OutlinedTextField(
                    value = solution, onValueChange = { solution = it },
                    label = { Text("Solución aplicada (opcional)") },
                    singleLine = false, minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onFinishTask(solution.ifBlank { null }); showFinishDialog = false }) { Text("Finalizar") }
            },
            dismissButton = { TextButton(onClick = { showFinishDialog = false }) { Text("Cancelar") } }
        )
    }

    // ── Diálogo: eliminar tarea ──────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar tarea") },
            text  = { Text("¿Eliminar esta tarea? Solo es posible si no está pagada.") },
            confirmButton = { TextButton(onClick = { viewModel.onDeleteTask(); showDeleteDialog = false }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TaskContent(
    task: WorkshopTask, role: UserRole,
    onAddHoursClick: () -> Unit, onFinishClick: () -> Unit, onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = when (task.status) {
        TaskStatus.PENDING     -> StatusPendingColor
        TaskStatus.IN_PROGRESS -> StatusInProgressColor
        TaskStatus.FINISHED    -> StatusFinishedColor
        TaskStatus.PAID        -> StatusPaidColor
    }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Estado como chip coloreado
        Surface(color = chipColor, shape = MaterialTheme.shapes.medium) {
            Text(task.status.label, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }

        InfoRow("Vehículo",       task.vehicleReg)
        InfoRow("Cliente",        task.clientName)
        InfoRow("Mecánico",       task.mechanicName)
        InfoRow("Diagnóstico",    task.diagnostic)
        task.solution?.let { InfoRow("Solución", it) }
        InfoRow("Fecha de inicio",task.initDate.toString())
        InfoRow("Horas estimadas","%.1f h".format(task.previewHours))
        InfoRow("Horas reales",   "%.1f h".format(task.realHours))
        InfoRow("Progreso",       "%.0f%%".format(task.progress))
        InfoRow("Coste estimado", "%.2f €".format(task.estimatedCost))
        if (task.finished) InfoRow("Coste total", "%.2f €".format(task.totalCost))
        task.notes?.let { InfoRow("Notas", it) }

        Spacer(Modifier.height(8.dp))

        // Acciones según rol y estado de la tarea
        if (role != UserRole.CLIENT && !task.finished) {
            Button(onClick = onAddHoursClick, modifier = Modifier.fillMaxWidth()) { Text("Añadir horas") }
            Button(onClick = onFinishClick, modifier = Modifier.fillMaxWidth()) { Text("Finalizar tarea") }
        }
        if (role == UserRole.ADMIN && task.finished && !task.paid) {
            Button(onClick = onPayClick, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                Text("Marcar como pagada")
            }
        }
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
