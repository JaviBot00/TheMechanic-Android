package com.hotguy.workshopmanagement.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.SummaryReport
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.ui.components.ErrorBox
import com.hotguy.workshopmanagement.ui.components.LoadingBox

/**
 * Pantalla de dashboard — resumen estadístico del taller.
 * Solo accesible para ADMIN y MECHANIC (el NavGraph no la incluye para CLIENT).
 * Muestra métricas clave y accesos rápidos a cada sección, con visibilidad
 * condicional del reporte detallado según el rol.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToClients:   () -> Unit,
    onNavigateToVehicles:  () -> Unit,
    onNavigateToMechanics: () -> Unit,
    onNavigateToTasks:     () -> Unit,
    onNavigateToReport:    () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = viewModel::onLogout) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = uiState) {
            is DashboardUiState.Loading ->
                LoadingBox(Modifier.padding(padding))
            is DashboardUiState.Error ->
                ErrorBox(s.message, onRetry = viewModel::loadReport, modifier = Modifier.padding(padding))
            is DashboardUiState.Success ->
                DashboardContent(
                    report = s.report, role = s.role,
                    onNavigateToClients = onNavigateToClients,
                    onNavigateToVehicles = onNavigateToVehicles,
                    onNavigateToMechanics = onNavigateToMechanics,
                    onNavigateToTasks = onNavigateToTasks,
                    onNavigateToReport = onNavigateToReport,
                    modifier = Modifier.padding(padding)
                )
        }
    }
}

@Composable
private fun DashboardContent(
    report: SummaryReport, role: UserRole,
    onNavigateToClients: () -> Unit, onNavigateToVehicles: () -> Unit,
    onNavigateToMechanics: () -> Unit, onNavigateToTasks: () -> Unit,
    onNavigateToReport: () -> Unit, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Resumen del taller", style = MaterialTheme.typography.titleLarge)

        // Cuadrícula 2x2 de estadísticas
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Clientes",  report.totalClients.toString(),  Icons.Outlined.Person,       Modifier.weight(1f))
            StatCard("Mecánicos", report.totalMechanics.toString(), Icons.Outlined.Build,        Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Vehículos",     report.totalVehicles.toString(), Icons.Outlined.DirectionsCar, Modifier.weight(1f))
            StatCard("Tareas activas",report.pendingTasks.toString(),  Icons.Outlined.Assignment,    Modifier.weight(1f))
        }
        StatCard(
            label = "Facturación total",
            value = "%.2f €".format(report.totalRevenue),
            icon  = Icons.Outlined.ReceiptLong,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge)

        NavCard("Clientes",  Icons.Outlined.Person,        onNavigateToClients)
        NavCard("Vehículos", Icons.Outlined.DirectionsCar, onNavigateToVehicles)
        NavCard("Mecánicos", Icons.Outlined.Build,         onNavigateToMechanics)
        NavCard("Tareas",    Icons.Outlined.Assignment,    onNavigateToTasks)
        // El reporte detallado solo es accesible para ADMIN
        if (role == UserRole.ADMIN) {
            NavCard("Reporte detallado", Icons.Outlined.ReceiptLong, onNavigateToReport)
        }
    }
}

/** Tarjeta de estadística con icono, valor y etiqueta. */
@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Tarjeta navegable de acceso rápido. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
