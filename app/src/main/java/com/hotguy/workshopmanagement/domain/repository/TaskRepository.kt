package com.hotguy.workshopmanagement.domain.repository

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Filtros disponibles para la lista de tareas.
 * Mapean a los distintos endpoints del backend.
 */
enum class TaskFilter {
    /** Todas las tareas — GET /api/v1/tasks */
    ALL,
    /** Tareas no finalizadas — GET /api/v1/tasks/pending */
    PENDING,
    /** Tareas finalizadas pero no pagadas — GET /api/v1/tasks/unpaid */
    UNPAID
}

/**
 * Contrato del repositorio de tareas de taller.
 *
 * Las tareas tienen el ciclo de vida más rico de todos los agregados:
 * creación → horas de trabajo → finalización → pago. Cada transición
 * de estado tiene su propio método aquí.
 */
interface TaskRepository {

    /**
     * Stream paginado de tareas según el filtro indicado.
     *
     * @param filter Determina qué endpoint se llama y qué tareas se muestran.
     */
    fun getTasks(filter: TaskFilter = TaskFilter.ALL): Flow<PagingData<WorkshopTask>>

    /**
     * Stream paginado de tareas de un cliente concreto.
     * Usado por CLIENT para ver solo sus propias tareas.
     *
     * @param clientId Identificador del cliente.
     */
    fun getTasksByClient(clientId: Long): Flow<PagingData<WorkshopTask>>

    /**
     * Stream paginado de tareas asociadas a un vehículo concreto.
     *
     * @param vehicleId Identificador del vehículo.
     */
    fun getTasksByVehicle(vehicleId: Long): Flow<PagingData<WorkshopTask>>

    /**
     * Stream paginado de tareas asignadas a un mecánico concreto.
     *
     * @param mechanicId Identificador del mecánico.
     */
    fun getTasksByMechanic(mechanicId: Long): Flow<PagingData<WorkshopTask>>

    /**
     * Obtiene una tarea por su identificador técnico.
     *
     * @param id Identificador de la tarea.
     * @return [Result] con la [WorkshopTask] o con la excepción si no existe.
     */
    suspend fun getTaskById(id: Long): Result<WorkshopTask>

    /**
     * Crea una nueva orden de trabajo.
     *
     * @param vehicleId    Vehículo a reparar (determina también el cliente automáticamente).
     * @param mechanicId   Mecánico responsable.
     * @param diagnostic   Descripción del problema detectado.
     * @param previewHours Horas estimadas de trabajo.
     * @param initDate     Fecha de inicio.
     * @param notes        Notas adicionales (opcional).
     * @return [Result] con la [WorkshopTask] creada.
     */
    suspend fun createTask(
        vehicleId:    Long,
        mechanicId:   Long,
        diagnostic:   String,
        previewHours: Float,
        initDate:     LocalDate,
        notes:        String?
    ): Result<WorkshopTask>

    /**
     * Acumula horas de trabajo a una tarea.
     * Falla si la tarea ya está finalizada.
     *
     * @param id    Identificador de la tarea.
     * @param hours Horas a añadir (debe ser positivo).
     * @return [Result] con la [WorkshopTask] actualizada.
     */
    suspend fun addHours(id: Long, hours: Float): Result<WorkshopTask>

    /**
     * Actualiza el diagnóstico, horas estimadas o notas de una tarea.
     *
     * @param id           Identificador de la tarea.
     * @param diagnostic   Nuevo diagnóstico (opcional, null = no cambia).
     * @param previewHours Nuevas horas estimadas (opcional, null = no cambia).
     * @param notes        Nuevas notas (opcional, null = no cambia).
     * @return [Result] con la [WorkshopTask] actualizada.
     */
    suspend fun updateTask(
        id:           Long,
        diagnostic:   String?,
        previewHours: Float?,
        notes:        String?
    ): Result<WorkshopTask>

    /**
     * Marca una tarea como finalizada.
     *
     * @param id       Identificador de la tarea.
     * @param solution Descripción de la solución aplicada (opcional).
     * @return [Result] con la [WorkshopTask] actualizada.
     */
    suspend fun finishTask(id: Long, solution: String?): Result<WorkshopTask>

    /**
     * Marca una tarea como pagada. Solo ADMIN.
     * Falla si la tarea no está finalizada aún.
     *
     * @param id Identificador de la tarea.
     * @return [Result] con la [WorkshopTask] actualizada.
     */
    suspend fun markAsPaid(id: Long): Result<WorkshopTask>

    /**
     * Elimina una tarea permanentemente. Solo ADMIN.
     * Falla si la tarea ya está pagada.
     *
     * @param id Identificador de la tarea a eliminar.
     * @return [Result.success] si se eliminó correctamente.
     */
    suspend fun deleteTask(id: Long): Result<Unit>
}
