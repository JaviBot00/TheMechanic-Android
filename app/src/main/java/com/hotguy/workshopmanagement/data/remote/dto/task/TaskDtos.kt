package com.hotguy.workshopmanagement.data.remote.dto.task

import com.hotguy.workshopmanagement.domain.model.TaskStatus
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

/**
 * DTO de respuesta para una tarea de taller.
 * Mapea el record [WorkshopTaskResponse] del servidor.
 */
@Serializable
data class WorkshopTaskDto(
    @SerialName("id")            val id:            Long,
    @SerialName("diagnostic")    val diagnostic:    String,
    @SerialName("solution")      val solution:      String? = null,
    @SerialName("previewHours")  val previewHours:  Float,
    @SerialName("realHours")     val realHours:     Float,
    @SerialName("progress")      val progress:      Float,
    // "Pendiente" | "En progreso" | "Finalizada" | "Pagada"
    @SerialName("status")        val status:        String,
    @SerialName("estimatedCost") val estimatedCost: Float,
    @SerialName("totalCost")     val totalCost:     Float,
    @SerialName("finished")      val finished:      Boolean,
    @SerialName("paid")          val paid:          Boolean,
    // LocalDate llega como "yyyy-MM-dd"
    @SerialName("initDate")      val initDate:      String,
    @SerialName("notes")         val notes:         String? = null,
    @SerialName("clientId")      val clientId:      Long,
    @SerialName("clientName")    val clientName:    String,
    @SerialName("vehicleId")     val vehicleId:     Long,
    @SerialName("vehicleReg")    val vehicleReg:    String,
    @SerialName("mechanicId")    val mechanicId:    Long,
    @SerialName("mechanicName")  val mechanicName:  String,
    @SerialName("createdAt")     val createdAt:     String,
    @SerialName("updatedAt")     val updatedAt:     String
)

/**
 * DTO de petición para crear una nueva tarea.
 * Mapea el record [WorkshopTaskRequest] del servidor.
 */
@Serializable
data class WorkshopTaskRequestDto(
    @SerialName("vehicleId")    val vehicleId:    Long,
    @SerialName("mechanicId")   val mechanicId:   Long,
    @SerialName("diagnostic")   val diagnostic:   String,
    @SerialName("previewHours") val previewHours: Float,
    @SerialName("initDate")     val initDate:     String,   // "yyyy-MM-dd"
    @SerialName("notes")        val notes:        String? = null
)

/**
 * DTO de petición para añadir horas a una tarea.
 * Mapea el record [AddHoursRequest] del servidor.
 */
@Serializable
data class AddHoursRequestDto(
    @SerialName("hours") val hours: Float
)

// ── Mapper ────────────────────────────────────────────────────────────────────

fun WorkshopTaskDto.toDomain(): WorkshopTask = WorkshopTask(
    id            = id,
    diagnostic    = diagnostic,
    solution      = solution,
    previewHours  = previewHours,
    realHours     = realHours,
    progress      = progress,
    status        = TaskStatus.fromLabel(status),
    estimatedCost = estimatedCost,
    totalCost     = totalCost,
    finished      = finished,
    paid          = paid,
    initDate      = LocalDate.parse(initDate),
    notes         = notes,
    clientId      = clientId,
    clientName    = clientName,
    vehicleId     = vehicleId,
    vehicleReg    = vehicleReg,
    mechanicId    = mechanicId,
    mechanicName  = mechanicName,
    createdAt     = Instant.parse(createdAt),
    updatedAt     = Instant.parse(updatedAt)
)
