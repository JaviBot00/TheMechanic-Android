package com.hotguy.workshopmanagement.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.di.SessionState
import com.hotguy.workshopmanagement.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Grafo de navegación principal de la aplicación.
 *
 * Gestiona dos responsabilidades:
 *
 * 1. **Destino inicial según sesión**: observa el [SessionState] del
 *    [NavViewModel] para decidir dónde aterriza el usuario al abrir la app
 *    y para navegar automáticamente al login cuando la sesión expira.
 *
 * 2. **Definición de rutas**: declara cada pantalla de la app vinculada
 *    a su ruta type-safe ([AppRoutes]). Las rutas serializables permiten
 *    pasar argumentos con seguridad de tipos sin cadenas literales.
 *
 * Flujo de sesión:
 * - [SessionState.Unauthenticated]  → destino inicial = Login.
 * - Login correcto                  → navegar a Dashboard o Mis Vehículos
 *                                     según rol, eliminar Login de la pila.
 * - Token expirado (TokenAuthenticator hace logout) → SessionState vuelve
 *   a Unauthenticated → la UI navega a Login desde cualquier pantalla.
 *
 * @param navController Controlador de navegación. Inyectable en tests.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val viewModel: NavViewModel = hiltViewModel()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    // Destino inicial: si ya hay sesión activa (tokens válidos del arranque
    // anterior), SessionManager los restaura antes de que se dibuje el NavHost
    // y sessionState ya llega como Authenticated aquí.
    // val startDestination: AppRoutes = when (val state = sessionState) {
    // Más seguro — evita problemas de inferencia en NavHost
    val startDestination: Any = when (val state = sessionState) {
        is SessionState.Unauthenticated -> AppRoutes.Login
        is SessionState.Authenticated   -> homeDestinationFor(state.role)
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ── Autenticación ────────────────────────────────────────────────────

        composable<AppRoutes.Login> {
            // LoginScreen se implementa en el Paso 8.
            // La lambda onLoginSuccess recibe el rol y navega al destino
            // correcto eliminando Login de la pila de retroceso.
            com.hotguy.workshopmanagement.ui.auth.LoginScreen(
                onLoginSuccess = { role ->
                    navController.navigate(homeDestinationFor(role)) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ────────────────────────────────────────────────────────

        composable<AppRoutes.Dashboard> {
            com.hotguy.workshopmanagement.ui.dashboard.DashboardScreen(
                onNavigateToClients   = { navController.navigate(AppRoutes.ClientList) },
                onNavigateToVehicles  = { navController.navigate(AppRoutes.VehicleList) },
                onNavigateToMechanics = { navController.navigate(AppRoutes.MechanicList) },
                onNavigateToTasks     = { navController.navigate(AppRoutes.TaskList()) },
                onNavigateToReport    = { navController.navigate(AppRoutes.Report) }
            )
        }

        // ── Clientes ─────────────────────────────────────────────────────────

        composable<AppRoutes.ClientList> {
            com.hotguy.workshopmanagement.ui.client.list.ClientListScreen(
                onClientClick = { id -> navController.navigate(AppRoutes.ClientDetail(id)) },
                onCreateClick = { navController.navigate(AppRoutes.ClientForm()) },
                onBackClick   = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.ClientDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.ClientDetail>()
            com.hotguy.workshopmanagement.ui.client.detail.ClientDetailScreen(
                clientId       = route.clientId,
                onEditClick    = { navController.navigate(AppRoutes.ClientForm(route.clientId)) },
                onVehicleClick = { vehicleId -> navController.navigate(AppRoutes.VehicleDetail(vehicleId)) },
                onBackClick    = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.ClientForm> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.ClientForm>()
            // clientId null → crear nuevo cliente; non-null → editar existente
            com.hotguy.workshopmanagement.ui.client.form.ClientFormScreen(
                clientId      = route.clientId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick   = { navController.popBackStack() }
            )
        }

        // ── Vehículos ────────────────────────────────────────────────────────

        composable<AppRoutes.VehicleList> {
            com.hotguy.workshopmanagement.ui.vehicle.list.VehicleListScreen(
                clientId       = null,
                onVehicleClick = { id -> navController.navigate(AppRoutes.VehicleDetail(id)) },
                onCreateClick  = { navController.navigate(AppRoutes.VehicleForm()) },
                onBackClick    = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.VehicleListByClient> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.VehicleListByClient>()
            // clientId = -1L es el centinela para "vehículos del usuario actual":
            // VehicleListScreen lo detecta y usa el ID del SessionManager.
            com.hotguy.workshopmanagement.ui.vehicle.list.VehicleListScreen(
                clientId       = route.clientId,
                onVehicleClick = { id -> navController.navigate(AppRoutes.VehicleDetail(id)) },
                onCreateClick  = { navController.navigate(AppRoutes.VehicleForm(clientId = route.clientId)) },
                onBackClick    = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.VehicleDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.VehicleDetail>()
            com.hotguy.workshopmanagement.ui.vehicle.detail.VehicleDetailScreen(
                vehicleId    = route.vehicleId,
                onEditClick  = { navController.navigate(AppRoutes.VehicleForm(route.vehicleId)) },
                onTaskClick  = { taskId -> navController.navigate(AppRoutes.TaskDetail(taskId)) },
                onCreateTask = { navController.navigate(AppRoutes.TaskForm(route.vehicleId)) },
                onBackClick  = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.VehicleForm> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.VehicleForm>()
            com.hotguy.workshopmanagement.ui.vehicle.form.VehicleFormScreen(
                vehicleId           = route.vehicleId,
                preselectedClientId = route.clientId,
                onSaveSuccess       = { navController.popBackStack() },
                onBackClick         = { navController.popBackStack() }
            )
        }

        // ── Mecánicos ────────────────────────────────────────────────────────

        composable<AppRoutes.MechanicList> {
            com.hotguy.workshopmanagement.ui.mechanic.list.MechanicListScreen(
                onMechanicClick = { id -> navController.navigate(AppRoutes.MechanicDetail(id)) },
                onCreateClick   = { navController.navigate(AppRoutes.MechanicForm()) },
                onBackClick     = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.MechanicDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.MechanicDetail>()
            com.hotguy.workshopmanagement.ui.mechanic.detail.MechanicDetailScreen(
                mechanicId  = route.mechanicId,
                onEditClick = { navController.navigate(AppRoutes.MechanicForm(route.mechanicId)) },
                onTaskClick = { taskId -> navController.navigate(AppRoutes.TaskDetail(taskId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.MechanicForm> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.MechanicForm>()
            com.hotguy.workshopmanagement.ui.mechanic.form.MechanicFormScreen(
                mechanicId    = route.mechanicId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick   = { navController.popBackStack() }
            )
        }

        // ── Tareas ───────────────────────────────────────────────────────────

        composable<AppRoutes.TaskList> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.TaskList>()
            com.hotguy.workshopmanagement.ui.task.list.TaskListScreen(
                filter        = route.filter,
                onTaskClick   = { id -> navController.navigate(AppRoutes.TaskDetail(id)) },
                onCreateClick = { navController.navigate(AppRoutes.TaskForm()) },
                onBackClick   = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.TaskDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.TaskDetail>()
            com.hotguy.workshopmanagement.ui.task.detail.TaskDetailScreen(
                taskId      = route.taskId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AppRoutes.TaskForm> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.TaskForm>()
            com.hotguy.workshopmanagement.ui.task.form.TaskFormScreen(
                preselectedVehicleId = route.vehicleId,
                onSaveSuccess        = { navController.popBackStack() },
                onBackClick          = { navController.popBackStack() }
            )
        }

        // ── Reporte ──────────────────────────────────────────────────────────

        composable<AppRoutes.Report> {
            com.hotguy.workshopmanagement.ui.report.ReportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Determina el destino de inicio según el rol del usuario.
 *
 * - ADMIN / MECHANIC → [AppRoutes.Dashboard] con estadísticas del taller.
 * - CLIENT → [AppRoutes.VehicleListByClient] con clientId = -1L (centinela
 *   que indica "cargar el clientId desde el SessionManager").
 */
private fun homeDestinationFor(role: UserRole): AppRoutes = when (role) {
    UserRole.CLIENT -> AppRoutes.VehicleListByClient(clientId = -1L)
    else            -> AppRoutes.Dashboard
}

// ── NavViewModel ──────────────────────────────────────────────────────────────

/**
 * ViewModel del grafo de navegación.
 *
 * Su única responsabilidad es exponer el [SessionManager.sessionState] como
 * un Flow que [AppNavGraph] puede observar de forma lifecycle-aware.
 *
 * Es un [@HiltViewModel] para que Hilt gestione su ciclo de vida y le inyecte
 * el [SessionManager] singleton. Al estar atado al NavHost (y no a una
 * pantalla concreta), sobrevive a la navegación entre pantallas pero se
 * destruye si el proceso muere.
 */
@HiltViewModel
class NavViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {

    /** Estado de sesión observable. Emite cada vez que el usuario hace login o logout. */
    val sessionState = sessionManager.sessionState
}
