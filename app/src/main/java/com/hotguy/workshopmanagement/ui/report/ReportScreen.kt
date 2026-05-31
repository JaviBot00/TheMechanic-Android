package com.hotguy.workshopmanagement.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.SummaryReport
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/**
 * Pantalla de reporte estadístico detallado.
 * Solo accesible para ADMIN y MECHANIC.
 * Muestra todas las métricas del taller en formato de tarjetas individuales,
 * con más detalle que el widget de resumen del Dashboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBackClick: () -> Unit,
    viewModel:   ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte del taller") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = uiState) {
            is ReportUiState.Loading ->
                LoadingBox(Modifier.padding(padding))
            is ReportUiState.Error   ->
                ErrorBox(
                    message  = s.message,
                    onRetry  = viewModel::loadReport,
                    modifier = Modifier.padding(padding)
                )
            is ReportUiState.Success ->
                ReportContent(report = s.report, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun ReportContent(report: SummaryReport, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Estadísticas generales", style = MaterialTheme.typography.titleLarge)

        ReportCard("Clientes activos",        report.totalClients.toString())
        ReportCard("Mecánicos activos",       report.totalMechanics.toString())
        ReportCard("Vehículos registrados",   report.totalVehicles.toString())
        ReportCard("Total de tareas",         report.totalTasks.toString())
        ReportCard("Tareas pendientes",       report.pendingTasks.toString())
        ReportCard(
            label = "Tareas completadas",
            value = (report.totalTasks - report.pendingTasks).toString()
        )

        Spacer(Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        Text("Facturación", style = MaterialTheme.typography.titleLarge)
        ReportCard(
            label = "Ingresos totales (tareas pagadas)",
            value = "%.2f €".format(report.totalRevenue)
        )
    }
}

/**
 * Tarjeta de métrica individual con etiqueta en gris y valor en grande.
 *
 * @param label Descripción de la métrica.
 * @param value Valor a mostrar.
 */
@Composable
private fun ReportCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier              = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text     = label,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
