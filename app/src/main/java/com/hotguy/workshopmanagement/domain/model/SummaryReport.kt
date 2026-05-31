package com.hotguy.workshopmanagement.domain.model

/**
 * Modelo de dominio que representa el resumen estadístico del taller.
 *
 * Lo consume el Dashboard (ADMIN y MECHANIC) para mostrar los indicadores
 * principales del estado del negocio.
 *
 * @param totalClients   Número total de clientes activos en el sistema.
 * @param totalMechanics Número total de mecánicos activos.
 * @param totalVehicles  Número total de vehículos registrados.
 * @param totalTasks     Número total de órdenes de trabajo.
 * @param pendingTasks   Tareas aún no finalizadas (en curso o sin empezar).
 * @param totalRevenue   Facturación total acumulada de tareas pagadas, en euros.
 */
data class SummaryReport(
    val totalClients:   Long,
    val totalMechanics: Long,
    val totalVehicles:  Long,
    val totalTasks:     Long,
    val pendingTasks:   Long,
    val totalRevenue:   Double
)
