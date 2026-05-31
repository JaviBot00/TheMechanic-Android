package com.hotguy.workshopmanagement.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Estados posibles de una tarea de taller.
 *
 * Se mapean desde el campo "status" (String) que devuelve el backend.
 * Tener un enum en dominio permite usar when() exhaustivo en la UI
 * para renderizar el color o el icono correcto para cada estado.
 *
 * @param label Texto legible tal como lo devuelve el backend.
 */
enum class TaskStatus(val label: String) {
    PENDING("Pendiente"),
    IN_PROGRESS("En progreso"),
    FINISHED("Finalizada"),
    PAID("Pagada");

    companion object {
        /**
         * Convierte el string del backend al enum. Usa [PENDING] como fallback.
         */
        fun fromLabel(label: String): TaskStatus =
            entries.firstOrNull { it.label == label } ?: PENDING
    }
}

/**
 * Modelo de dominio que representa una orden de trabajo del taller.
 *
 * Una tarea vincula un vehículo, un mecánico y un cliente, y gestiona el
 * ciclo de vida completo de una reparación: diagnóstico → trabajo en progreso
 * → finalización → cobro.
 *
 * Los costes (estimado y real) vienen ya calculados desde el backend para
 * evitar duplicar la lógica de tarifas en el cliente.
 *
 * @param id            Identificador técnico interno.
 * @param diagnostic    Descripción del problema detectado.
 * @param solution      Descripción de la solución aplicada (null si aún no finalizada).
 * @param previewHours  Horas estimadas para completar la tarea.
 * @param realHours     Horas reales trabajadas hasta el momento.
 * @param progress      Porcentaje de progreso respecto a las horas estimadas (0–100).
 * @param status        Estado actual de la tarea como enum [TaskStatus].
 * @param estimatedCost Coste según horas presupuestadas, en euros.
 * @param totalCost     Coste real según horas trabajadas, en euros (0 si no finalizada).
 * @param finished      true cuando la tarea ha sido marcada como completada.
 * @param paid          true cuando el cliente ha abonado la factura.
 * @param initDate      Fecha de inicio de la tarea.
 * @param notes         Notas adicionales del mecánico (pueden ser null).
 * @param clientId      Identificador del cliente propietario.
 * @param clientName    Nombre del cliente (incluido para evitar joins en UI).
 * @param vehicleId     Identificador del vehículo en reparación.
 * @param vehicleReg    Matrícula del vehículo.
 * @param mechanicId    Identificador del mecánico responsable.
 * @param mechanicName  Nombre del mecánico.
 * @param createdAt     Fecha de creación del registro.
 * @param updatedAt     Fecha de la última modificación.
 */
data class WorkshopTask(
    val id:            Long,
    val diagnostic:    String,
    val solution:      String?,
    val previewHours:  Float,
    val realHours:     Float,
    val progress:      Float,
    val status:        TaskStatus,
    val estimatedCost: Float,
    val totalCost:     Float,
    val finished:      Boolean,
    val paid:          Boolean,
    val initDate:      LocalDate,
    val notes:         String?,
    val clientId:      Long,
    val clientName:    String,
    val vehicleId:     Long,
    val vehicleReg:    String,
    val mechanicId:    Long,
    val mechanicName:  String,
    val createdAt:     Instant,
    val updatedAt:     Instant
)
