package com.hotguy.workshopmanagement.data.remote.dto.report

import com.hotguy.workshopmanagement.domain.model.SummaryReport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de respuesta para el resumen estadístico del taller.
 * Mapea el record [SummaryReportResponse] del servidor.
 */
@Serializable
data class SummaryReportDto(
    @SerialName("totalClients")   val totalClients:   Long,
    @SerialName("totalMechanics") val totalMechanics: Long,
    @SerialName("totalVehicles")  val totalVehicles:  Long,
    @SerialName("totalTasks")     val totalTasks:     Long,
    @SerialName("pendingTasks")   val pendingTasks:   Long,
    @SerialName("totalRevenue")   val totalRevenue:   Double
)

// ── Mapper ────────────────────────────────────────────────────────────────────

fun SummaryReportDto.toDomain(): SummaryReport = SummaryReport(
    totalClients   = totalClients,
    totalMechanics = totalMechanics,
    totalVehicles  = totalVehicles,
    totalTasks     = totalTasks,
    pendingTasks   = pendingTasks,
    totalRevenue   = totalRevenue
)
