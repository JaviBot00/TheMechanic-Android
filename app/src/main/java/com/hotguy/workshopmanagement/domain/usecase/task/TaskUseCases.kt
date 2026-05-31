package com.hotguy.workshopmanagement.domain.usecase.task

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import com.hotguy.workshopmanagement.domain.repository.TaskFilter
import com.hotguy.workshopmanagement.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use Case: obtener el listado paginado de tareas con un filtro opcional.
 *
 * El [TaskFilter] determina qué endpoint del backend se usa:
 * ALL → todas, PENDING → sin finalizar, UNPAID → finalizadas sin pagar.
 */
class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(filter: TaskFilter = TaskFilter.ALL): Flow<PagingData<WorkshopTask>> =
        taskRepository.getTasks(filter)
}

/**
 * Use Case: obtener las tareas de un cliente concreto.
 * Usado por CLIENT para ver solo sus propias tareas, y por ADMIN/MECHANIC
 * al ver el detalle de un cliente.
 */
class GetTasksByClientUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(clientId: Long): Flow<PagingData<WorkshopTask>> =
        taskRepository.getTasksByClient(clientId)
}

/**
 * Use Case: obtener las tareas asociadas a un vehículo concreto.
 */
class GetTasksByVehicleUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(vehicleId: Long): Flow<PagingData<WorkshopTask>> =
        taskRepository.getTasksByVehicle(vehicleId)
}

/**
 * Use Case: obtener las tareas asignadas a un mecánico concreto.
 */
class GetTasksByMechanicUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(mechanicId: Long): Flow<PagingData<WorkshopTask>> =
        taskRepository.getTasksByMechanic(mechanicId)
}

/**
 * Use Case: obtener el detalle de una tarea por su ID.
 */
class GetTaskByIdUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: Long): Result<WorkshopTask> = taskRepository.getTaskById(id)
}

/**
 * Use Case: crear una nueva orden de trabajo.
 *
 * Reglas de dominio:
 * - El diagnóstico no puede estar vacío.
 * - Las horas estimadas deben ser positivas.
 * - La fecha de inicio no puede ser nula.
 * - Debe seleccionarse un vehículo y un mecánico válidos.
 */
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        vehicleId:    Long,
        mechanicId:   Long,
        diagnostic:   String,
        previewHours: Float,
        initDate:     LocalDate,
        notes:        String?
    ): Result<WorkshopTask> {
        if (vehicleId <= 0)       return Result.failure(IllegalArgumentException("Debe seleccionar un vehículo"))
        if (mechanicId <= 0)      return Result.failure(IllegalArgumentException("Debe seleccionar un mecánico"))
        if (diagnostic.isBlank()) return Result.failure(IllegalArgumentException("El diagnóstico es obligatorio"))
        if (previewHours <= 0f)   return Result.failure(IllegalArgumentException("Las horas estimadas deben ser positivas"))

        return taskRepository.createTask(
            vehicleId    = vehicleId,
            mechanicId   = mechanicId,
            diagnostic   = diagnostic.trim(),
            previewHours = previewHours,
            initDate     = initDate,
            notes        = notes?.trim()
        )
    }
}

/**
 * Use Case: añadir horas de trabajo a una tarea en curso.
 *
 * Regla de dominio: las horas a añadir deben ser estrictamente positivas.
 * El servidor también lo valida, pero damos feedback inmediato sin llamada
 * de red si el valor es incorrecto.
 *
 * @param id    Identificador de la tarea.
 * @param hours Horas a añadir. Debe ser > 0.
 */
class AddHoursToTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: Long, hours: Float): Result<WorkshopTask> {
        if (hours <= 0f) return Result.failure(
            IllegalArgumentException("Las horas deben ser un valor positivo")
        )
        return taskRepository.addHours(id, hours)
    }
}

/**
 * Use Case: marcar una tarea como finalizada.
 *
 * @param id       Identificador de la tarea.
 * @param solution Descripción de la solución aplicada (opcional).
 */
class FinishTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: Long, solution: String?): Result<WorkshopTask> =
        taskRepository.finishTask(id, solution?.trim())
}

/**
 * Use Case: marcar una tarea como pagada.
 *
 * Esta es la operación más restringida del sistema: solo ADMIN puede ejecutarla.
 * La regla de negocio crítica (la tarea debe estar finalizada antes de marcarla
 * como pagada) la impone el servidor y devuelve un error 409 si no se cumple.
 * El ViewModel convierte ese error en un mensaje comprensible para el usuario.
 *
 * @param id Identificador de la tarea a cobrar.
 */
class MarkTaskAsPaidUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: Long): Result<WorkshopTask> = taskRepository.markAsPaid(id)
}

/**
 * Use Case: actualizar el diagnóstico, horas estimadas o notas de una tarea.
 *
 * @param id           Identificador de la tarea.
 * @param diagnostic   Nuevo texto de diagnóstico (null = no cambia).
 * @param previewHours Nuevas horas estimadas (null = no cambia).
 * @param notes        Nuevas notas (null = no cambia).
 */
class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        id:           Long,
        diagnostic:   String? = null,
        previewHours: Float?  = null,
        notes:        String? = null
    ): Result<WorkshopTask> {
        if (diagnostic != null && diagnostic.isBlank()) return Result.failure(
            IllegalArgumentException("El diagnóstico no puede estar vacío")
        )
        if (previewHours != null && previewHours <= 0f) return Result.failure(
            IllegalArgumentException("Las horas estimadas deben ser positivas")
        )
        return taskRepository.updateTask(id, diagnostic?.trim(), previewHours, notes?.trim())
    }
}

/**
 * Use Case: eliminar una tarea permanentemente.
 * Solo ADMIN. El servidor rechaza la operación si la tarea ya está pagada.
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = taskRepository.deleteTask(id)
}
