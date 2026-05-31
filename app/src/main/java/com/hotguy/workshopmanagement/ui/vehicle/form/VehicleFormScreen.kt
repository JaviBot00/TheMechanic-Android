package com.hotguy.workshopmanagement.ui.vehicle.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.VehicleType
import com.hotguy.workshopmanagement.ui.components.PrimaryButton
import com.hotguy.workshopmanagement.ui.components.WorkshopTextField

/** Formulario de creación y edición de vehículos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormScreen(
    vehicleId: Long?, preselectedClientId: Long?,
    onSaveSuccess: () -> Unit, onBackClick: () -> Unit,
    viewModel: VehicleFormViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { when (it) { is VehicleFormEvent.SavedSuccessfully -> onSaveSuccess() } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.isEditMode) "Editar vehículo" else "Nuevo vehículo") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WorkshopTextField(s.registrationCode, viewModel::onRegistrationCodeChange, "Matrícula")
            WorkshopTextField(s.model,            viewModel::onModelChange,            "Marca y modelo")
            WorkshopTextField(s.clientId,         viewModel::onClientIdChange,         "ID del propietario")

            // Selector de tipo de vehículo
            Text("Tipo de vehículo", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            VehicleType.entries.forEach { type ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = s.type == type, onClick = { viewModel.onTypeChange(type) })
                    Text("${type.labelEs} (${type.hourlyRate}€/h + ${type.fixedFee}€ fijo)")
                }
            }

            if (s.errorMessage != null) {
                Text(s.errorMessage!!, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Guardar", viewModel::onSaveClick, isLoading = s.isLoading)
        }
    }
}
