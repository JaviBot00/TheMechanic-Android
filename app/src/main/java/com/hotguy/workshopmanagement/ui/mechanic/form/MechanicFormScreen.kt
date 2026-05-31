package com.hotguy.workshopmanagement.ui.mechanic.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.ui.components.PrimaryButton
import com.hotguy.workshopmanagement.ui.components.WorkshopTextField

/** Formulario de creación y edición de mecánicos. Solo accesible para ADMIN. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicFormScreen(
    mechanicId: Long?, onSaveSuccess: () -> Unit, onBackClick: () -> Unit,
    viewModel: MechanicFormViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { when (it) { is MechanicFormEvent.SavedSuccessfully -> onSaveSuccess() } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.isEditMode) "Editar mecánico" else "Nuevo mecánico") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WorkshopTextField(s.name,             viewModel::onNameChange,             "Nombre")
            WorkshopTextField(s.surname1,         viewModel::onSurname1Change,         "Primer apellido")
            WorkshopTextField(s.surname2,         viewModel::onSurname2Change,         "Segundo apellido (opcional)")
            WorkshopTextField(s.nif,              viewModel::onNifChange,              "NIF")
            WorkshopTextField(s.email,            viewModel::onEmailChange,            "Email")
            WorkshopTextField(s.telephone,        viewModel::onTelephoneChange,        "Teléfono (opcional)")
            WorkshopTextField(s.registrationDate, viewModel::onRegistrationDateChange, "Fecha de alta (yyyy-MM-dd)")
            WorkshopTextField(s.specialty,        viewModel::onSpecialtyChange,        "Especialidad")

            if (s.errorMessage != null) {
                Text(s.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Guardar", viewModel::onSaveClick, isLoading = s.isLoading)
        }
    }
}
