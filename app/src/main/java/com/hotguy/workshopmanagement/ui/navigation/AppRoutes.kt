package com.hotguy.workshopmanagement.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Definición de rutas de navegación type-safe.
 *
 * Navigation Compose 2.8+ soporta rutas basadas en clases Kotlin serializables
 * en lugar de cadenas de texto planas. Esto elimina los errores de typo en los
 * nombres de ruta y permite pasar argumentos de forma completamente tipada.
 *
 * Cada objeto o data class anotado con [@Serializable] representa una pantalla
 * en el grafo de navegación. Los argumentos de navegación son campos del
 * data class — Navigation Compose los serializa/deserializa automáticamente.
 *
 * Uso en el NavGraph:
 *   composable<AppRoutes.ClientDetail> { backStack ->
 *       val route = backStack.toRoute<AppRoutes.ClientDetail>()
 *       ClientDetailScreen(clientId = route.clientId)
 *   }
 *
 * Uso para navegar:
 *   navController.navigate(AppRoutes.ClientDetail(clientId = 42L))
 */
sealed interface AppRoutes {

    // ── Autenticación ────────────────────────────────────────────────────────

    /** Pantalla de inicio de sesión. Destino inicial cuando no hay sesión activa. */
    @Serializable
    data object Login : AppRoutes

    // ── Dashboard ────────────────────────────────────────────────────────────

    /**
     * Panel de resumen del taller.
     * Destino inicial para ADMIN y MECHANIC tras el login.
     */
    @Serializable
    data object Dashboard : AppRoutes

    // ── Clientes ─────────────────────────────────────────────────────────────

    /** Lista paginada de todos los clientes activos. */
    @Serializable
    data object ClientList : AppRoutes

    /**
     * Detalle completo de un cliente.
     * @param clientId Identificador del cliente a mostrar.
     */
    @Serializable
    data class ClientDetail(val clientId: Long) : AppRoutes

    /**
     * Formulario de alta o edición de cliente.
     * @param clientId Si es null, se está creando un cliente nuevo.
     *                 Si tiene valor, se está editando el cliente con ese id.
     */
    @Serializable
    data class ClientForm(val clientId: Long? = null) : AppRoutes

    // ── Vehículos ────────────────────────────────────────────────────────────

    /** Lista de todos los vehículos. */
    @Serializable
    data object VehicleList : AppRoutes

    /**
     * Lista de vehículos filtrada por cliente.
     * Usada en la vista de CLIENT (solo sus propios vehículos).
     * @param clientId Identificador del propietario.
     */
    @Serializable
    data class VehicleListByClient(val clientId: Long) : AppRoutes

    /**
     * Detalle de un vehículo con su historial de reparaciones.
     * @param vehicleId Identificador del vehículo.
     */
    @Serializable
    data class VehicleDetail(val vehicleId: Long) : AppRoutes

    /**
     * Formulario de alta o edición de vehículo.
     * @param vehicleId Si es null, vehículo nuevo. Con valor, edición.
     * @param clientId  Pre-selecciona el propietario al crear desde el detalle de un cliente.
     */
    @Serializable
    data class VehicleForm(val vehicleId: Long? = null, val clientId: Long? = null) : AppRoutes

    // ── Mecánicos ────────────────────────────────────────────────────────────

    /** Lista de todos los mecánicos activos. */
    @Serializable
    data object MechanicList : AppRoutes

    /**
     * Detalle de un mecánico con sus tareas asignadas.
     * @param mechanicId Identificador del mecánico.
     */
    @Serializable
    data class MechanicDetail(val mechanicId: Long) : AppRoutes

    /**
     * Formulario de alta o edición de mecánico. Solo ADMIN.
     * @param mechanicId Si es null, mecánico nuevo. Con valor, edición.
     */
    @Serializable
    data class MechanicForm(val mechanicId: Long? = null) : AppRoutes

    // ── Tareas de taller ─────────────────────────────────────────────────────

    /**
     * Lista de tareas con filtro opcional.
     * @param filter "ALL" | "PENDING" | "UNPAID" — determina qué endpoint se llama.
     */
    @Serializable
    data class TaskList(val filter: String = "ALL") : AppRoutes

    /**
     * Detalle de una tarea con todas sus acciones disponibles.
     * @param taskId Identificador de la tarea.
     */
    @Serializable
    data class TaskDetail(val taskId: Long) : AppRoutes

    /**
     * Formulario de creación de tarea. Solo ADMIN y MECHANIC.
     * @param vehicleId Pre-selecciona el vehículo al crear desde el detalle de un vehículo.
     */
    @Serializable
    data class TaskForm(val vehicleId: Long? = null) : AppRoutes

    // ── Reporte ──────────────────────────────────────────────────────────────

    /** Pantalla de resumen estadístico del taller. Solo ADMIN y MECHANIC. */
    @Serializable
    data object Report : AppRoutes
}
