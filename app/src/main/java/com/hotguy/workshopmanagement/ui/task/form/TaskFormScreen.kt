package com.hotguy.workshopmanagement.ui.task.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.ui.components.PrimaryButton
import com.hotguy.workshopmanagement.ui.components.WorkshopTextField

/**
 * Formulario de creación de una nueva orden de trabajo.
 * Solo accesible para ADMIN y MECHANIC.
 * Si se navega desde el detalle de un vehículo, la matrícula/ID viene
 * pre-rellenada y el campo está bloqueado visualmente (solo lectura en el
 * ViewModel; el usuario puede cambiarlo si quiere).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    preselectedVehicleId: Long?,
    onSaveSuccess:        () -> Unit,
    onBackClick:          () -> Unit,
    viewModel:            TaskFormViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) { is TaskFormEvent.SavedSuccessfully -> onSaveSuccess() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva tarea") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WorkshopTextField(
                value         = s.vehicleId,
                onValueChange = viewModel::onVehicleIdChange,
                label         = "ID del vehículo"
            )
            WorkshopTextField(
                value         = s.mechanicId,
                onValueChange = viewModel::onMechanicIdChange,
                label         = "ID del mecánico"
            )
            WorkshopTextField(
                value         = s.diagnostic,
                onValueChange = viewModel::onDiagnosticChange,
                label         = "Diagnóstico",
                singleLine    = false
            )
            OutlinedTextField(
                value         = s.previewHours,
                onValueChange = viewModel::onPreviewHoursChange,
                label         = { Text("Horas estimadas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            WorkshopTextField(
                value         = s.initDate,
                onValueChange = viewModel::onInitDateChange,
                label         = "Fecha de inicio (yyyy-MM-dd)"
            )
            WorkshopTextField(
                value         = s.notes,
                onValueChange = viewModel::onNotesChange,
                label         = "Notas (opcional)",
                singleLine    = false
            )

            if (s.errorMessage != null) {
                Text(
                    text  = s.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text      = "Crear tarea",
                onClick   = viewModel::onSaveClick,
                isLoading = s.isLoading
            )
        }
    }
}
