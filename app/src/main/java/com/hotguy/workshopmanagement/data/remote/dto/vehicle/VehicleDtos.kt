package com.hotguy.workshopmanagement.data.remote.dto.vehicle

import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.domain.model.VehicleType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de respuesta para un vehículo.
 * Mapea el record [VehicleResponse] del servidor.
 */
@Serializable
data class VehicleDto(
    @SerialName("id")               val id:               Long,
    @SerialName("registrationCode") val registrationCode: String,
    @SerialName("model")            val model:            String,
    // El tipo llega como String ("CAR", "VAN"...) y se convierte al enum en el mapper
    @SerialName("type")             val type:             String,
    @SerialName("hourlyRate")       val hourlyRate:       Float,
    @SerialName("fixedFee")         val fixedFee:         Float,
    @SerialName("clientId")         val clientId:         Long,
    @SerialName("clientName")       val clientName:       String,
    @SerialName("taskCount")        val taskCount:        Int,
    @SerialName("completionPct")    val completionPct:    Float,
    @SerialName("totalRevenue")     val totalRevenue:     Float,
    @SerialName("createdAt")        val createdAt:        String,
    @SerialName("updatedAt")        val updatedAt:        String
)

/**
 * DTO de petición para crear o actualizar un vehículo.
 * Mapea el record [VehicleRequest] del servidor.
 */
@Serializable
data class VehicleRequestDto(
    @SerialName("registrationCode") val registrationCode: String,
    @SerialName("model")            val model:            String,
    @SerialName("type")             val type:             String,   // Se serializa el apiValue del enum
    @SerialName("clientId")         val clientId:         Long
)

// ── Mapper ────────────────────────────────────────────────────────────────────

fun VehicleDto.toDomain(): Vehicle = Vehicle(
    id               = id,
    registrationCode = registrationCode,
    model            = model,
    type             = VehicleType.fromApi(type),
    clientId         = clientId,
    clientName       = clientName,
    taskCount        = taskCount,
    completionPct    = completionPct,
    totalRevenue     = totalRevenue,
    createdAt        = java.time.Instant.parse(createdAt),
    updatedAt        = java.time.Instant.parse(updatedAt)
)
