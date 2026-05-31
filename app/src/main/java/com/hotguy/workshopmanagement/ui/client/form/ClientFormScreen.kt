package com.hotguy.workshopmanagement.ui.client.form

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

/**
 * Formulario de creación y edición de clientes.
 * El título de la barra y el botón cambian automáticamente según el modo
 * (crear vs editar) indicado por el ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen(
    clientId: Long?, onSaveSuccess: () -> Unit, onBackClick: () -> Unit,
    viewModel: ClientFormViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { when (it) { is ClientFormEvent.SavedSuccessfully -> onSaveSuccess() } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.isEditMode) "Editar cliente" else "Nuevo cliente") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Outlined.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WorkshopTextField(s.clientCode, viewModel::onClientCodeChange, "Código de cliente")
            WorkshopTextField(s.name,       viewModel::onNameChange,       "Nombre")
            WorkshopTextField(s.surname1,   viewModel::onSurname1Change,   "Primer apellido")
            WorkshopTextField(s.surname2,   viewModel::onSurname2Change,   "Segundo apellido (opcional)")
            WorkshopTextField(s.nif,        viewModel::onNifChange,        "NIF")
            WorkshopTextField(s.email,      viewModel::onEmailChange,      "Email")
            WorkshopTextField(s.telephone,  viewModel::onTelephoneChange,  "Teléfono (opcional)")

            if (s.errorMessage != null) {
                Text(s.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton("Guardar", viewModel::onSaveClick, isLoading = s.isLoading)
        }
    }
}
