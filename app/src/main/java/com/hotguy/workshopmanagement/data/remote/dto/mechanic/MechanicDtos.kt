package com.hotguy.workshopmanagement.data.remote.dto.mechanic

import com.hotguy.workshopmanagement.domain.model.Mechanic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

/**
 * DTO de respuesta para un mecánico.
 * Mapea el record [MechanicResponse] del servidor.
 */
@Serializable
data class MechanicDto(
    @SerialName("id")               val id:               Long,
    @SerialName("name")             val name:             String,
    @SerialName("surname1")         val surname1:         String,
    @SerialName("surname2")         val surname2:         String? = null,
    @SerialName("nif")              val nif:              String,
    @SerialName("email")            val email:            String,
    @SerialName("telephone")        val telephone:        String? = null,
    // LocalDate llega como String "yyyy-MM-dd" desde el backend (Jackson serializa así)
    @SerialName("registrationDate") val registrationDate: String,
    @SerialName("specialty")        val specialty:        String,
    @SerialName("taskCount")        val taskCount:        Int,
    @SerialName("createdAt")        val createdAt:        String,
    @SerialName("updatedAt")        val updatedAt:        String
)

/**
 * DTO de petición para crear o actualizar un mecánico.
 * Mapea el record [MechanicRequest] del servidor.
 */
@Serializable
data class MechanicRequestDto(
    @SerialName("name")             val name:             String,
    @SerialName("surname1")         val surname1:         String,
    @SerialName("surname2")         val surname2:         String? = null,
    @SerialName("nif")              val nif:              String,
    @SerialName("email")            val email:            String,
    @SerialName("telephone")        val telephone:        String? = null,
    @SerialName("registrationDate") val registrationDate: String,   // "yyyy-MM-dd"
    @SerialName("specialty")        val specialty:        String
)

// ── Mapper ────────────────────────────────────────────────────────────────────

fun MechanicDto.toDomain(): Mechanic = Mechanic(
    id               = id,
    name             = name,
    surname1         = surname1,
    surname2         = surname2,
    nif              = nif,
    email            = email,
    telephone        = telephone,
    registrationDate = LocalDate.parse(registrationDate),
    specialty        = specialty,
    taskCount        = taskCount,
    createdAt        = Instant.parse(createdAt),
    updatedAt        = Instant.parse(updatedAt)
)
