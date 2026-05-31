package com.hotguy.workshopmanagement.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hotguy.workshopmanagement.data.remote.api.MechanicApiService
import com.hotguy.workshopmanagement.data.remote.api.ReportApiService
import com.hotguy.workshopmanagement.data.remote.api.VehicleApiService
import com.hotguy.workshopmanagement.data.remote.api.WorkshopTaskApiService
import com.hotguy.workshopmanagement.data.remote.dto.mechanic.MechanicRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.mechanic.toDomain
import com.hotguy.workshopmanagement.data.remote.dto.report.toDomain
import com.hotguy.workshopmanagement.data.remote.dto.task.AddHoursRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.task.WorkshopTaskRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.task.toDomain
import com.hotguy.workshopmanagement.data.remote.dto.vehicle.VehicleRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.vehicle.toDomain
import com.hotguy.workshopmanagement.domain.model.Mechanic
import com.hotguy.workshopmanagement.domain.model.SummaryReport
import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.domain.model.VehicleType
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import com.hotguy.workshopmanagement.domain.repository.MechanicRepository
import com.hotguy.workshopmanagement.domain.repository.ReportRepository
import com.hotguy.workshopmanagement.domain.repository.TaskFilter
import com.hotguy.workshopmanagement.domain.repository.TaskRepository
import com.hotguy.workshopmanagement.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// VehicleRepositoryImpl
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Implementación concreta de [VehicleRepository].
 * Sigue el mismo patrón que [ClientRepositoryImpl]: Pager + GenericPagingSource
 * para listados, runCatching para operaciones puntuales.
 */
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleApiService: VehicleApiService
) : VehicleRepository {

    private companion object { const val PAGE_SIZE = 20 }

    override fun getVehicles(): Flow<PagingData<Vehicle>> = Pager(pagingConfig()) {
        GenericPagingSource { page ->
            vehicleApiService.getVehicles(page, PAGE_SIZE).content.map { it.toDomain() }
        }
    }.flow

    override fun getVehiclesByClient(clientId: Long): Flow<PagingData<Vehicle>> =
        Pager(pagingConfig()) {
            GenericPagingSource { page ->
                vehicleApiService.getVehiclesByClient(clientId, page, PAGE_SIZE)
                    .content.map { it.toDomain() }
            }
        }.flow

    override fun getVehiclesByType(type: VehicleType): Flow<PagingData<Vehicle>> =
        Pager(pagingConfig()) {
            GenericPagingSource { page ->
                vehicleApiService.getVehiclesByType(type.apiValue, page, PAGE_SIZE)
                    .content.map { it.toDomain() }
            }
        }.flow

    override suspend fun getVehicleById(id: Long): Result<Vehicle> = runCatching {
        vehicleApiService.getVehicleById(id).toDomain()
    }

    override suspend fun createVehicle(
        registrationCode: String,
        model: String,
        type: VehicleType,
        clientId: Long
    ): Result<Vehicle> = runCatching {
        vehicleApiService.createVehicle(
            VehicleRequestDto(registrationCode, model, type.apiValue, clientId)
        ).toDomain()
    }

    override suspend fun updateVehicle(
        id: Long,
        registrationCode: String,
        model: String,
        type: VehicleType,
        clientId: Long
    ): Result<Vehicle> = runCatching {
        vehicleApiService.updateVehicle(
            id      = id,
            request = VehicleRequestDto(registrationCode, model, type.apiValue, clientId)
        ).toDomain()
    }

    override suspend fun deleteVehicle(id: Long): Result<Unit> = runCatching {
        vehicleApiService.deleteVehicle(id)
    }

    private fun pagingConfig() = PagingConfig(
        pageSize = PAGE_SIZE, enablePlaceholders = false, prefetchDistance = 5
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// MechanicRepositoryImpl
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Implementación concreta de [MechanicRepository].
 */
class MechanicRepositoryImpl @Inject constructor(
    private val mechanicApiService: MechanicApiService
) : MechanicRepository {

    private companion object { const val PAGE_SIZE = 20 }

    override fun getMechanics(): Flow<PagingData<Mechanic>> = Pager(pagingConfig()) {
        GenericPagingSource { page ->
            mechanicApiService.getMechanics(page, PAGE_SIZE).content.map { it.toDomain() }
        }
    }.flow

    override fun getMechanicsBySpecialty(specialty: String): Flow<PagingData<Mechanic>> =
        Pager(pagingConfig()) {
            GenericPagingSource { page ->
                mechanicApiService.searchBySpecialty(specialty, page, PAGE_SIZE)
                    .content.map { it.toDomain() }
            }
        }.flow

    override suspend fun getMechanicById(id: Long): Result<Mechanic> = runCatching {
        mechanicApiService.getMechanicById(id).toDomain()
    }

    override suspend fun createMechanic(
        name: String, surname1: String, surname2: String?,
        nif: String, email: String, telephone: String?,
        registrationDate: LocalDate, specialty: String
    ): Result<Mechanic> = runCatching {
        mechanicApiService.createMechanic(
            MechanicRequestDto(name, surname1, surname2, nif, email, telephone,
                registrationDate.toString(), specialty)
        ).toDomain()
    }

    override suspend fun updateMechanic(
        id: Long, name: String, surname1: String, surname2: String?,
        nif: String, email: String, telephone: String?,
        registrationDate: LocalDate, specialty: String
    ): Result<Mechanic> = runCatching {
        mechanicApiService.updateMechanic(
            id      = id,
            request = MechanicRequestDto(name, surname1, surname2, nif, email, telephone,
                registrationDate.toString(), specialty)
        ).toDomain()
    }

    override suspend fun deleteMechanic(id: Long): Result<Unit> = runCatching {
        mechanicApiService.deleteMechanic(id)
    }

    private fun pagingConfig() = PagingConfig(
        pageSize = PAGE_SIZE, enablePlaceholders = false, prefetchDistance = 5
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// TaskRepositoryImpl
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Implementación concreta de [TaskRepository].
 *
 * El listado de tareas acepta un [TaskFilter] que determina qué endpoint
 * del backend se usa: ALL → /tasks, PENDING → /tasks/pending, UNPAID → /tasks/unpaid.
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskApiService: WorkshopTaskApiService
) : TaskRepository {

    private companion object { const val PAGE_SIZE = 20 }

    override fun getTasks(filter: TaskFilter): Flow<PagingData<WorkshopTask>> =
        Pager(pagingConfig()) {
            GenericPagingSource { page ->
                when (filter) {
                    TaskFilter.ALL     -> taskApiService.getTasks(page, PAGE_SIZE)
                    TaskFilter.PENDING -> taskApiService.getPendingTasks(page, PAGE_SIZE)
                    TaskFilter.UNPAID  -> taskApiService.getUnpaidTasks(page, PAGE_SIZE)
                }.content.map { it.toDomain() }
            }
        }.flow

    override fun getTasksByClient(clientId: Long): Flow<PagingData<WorkshopTask>> =
        Pager(pagingConfig()) {
            GenericPagingSource { page ->
                taskApiService.getTasksByClient(clientId, page, PAGE_SIZE)
                    .content.map { it.toDomain() }
            }
        }.flow

    override fun getTasksByVehicle(vehicleId: Long): Flow<PagingData<WorkshopTask>> =
        Pager(pagingConfig()) {
            GenericPagingSource { page ->
                taskApiService.getTasksByVehicle(vehicleId, page, PAGE_SIZE)
                    .content.map { it.toDomain() }
            }
        }.flow

    override fun getTasksByMechanic(mechanicId: Long): Flow<PagingData<WorkshopTask>> =
        Pager(pagingConfig()) {
            GenericPagingSource { page ->
                taskApiService.getTasksByMechanic(mechanicId, page, PAGE_SIZE)
                    .content.map { it.toDomain() }
            }
        }.flow

    override suspend fun getTaskById(id: Long): Result<WorkshopTask> = runCatching {
        taskApiService.getTaskById(id).toDomain()
    }

    override suspend fun createTask(
        vehicleId: Long, mechanicId: Long, diagnostic: String,
        previewHours: Float, initDate: LocalDate, notes: String?
    ): Result<WorkshopTask> = runCatching {
        taskApiService.createTask(
            WorkshopTaskRequestDto(vehicleId, mechanicId, diagnostic,
                previewHours, initDate.toString(), notes)
        ).toDomain()
    }

    override suspend fun addHours(id: Long, hours: Float): Result<WorkshopTask> = runCatching {
        taskApiService.addHours(id, AddHoursRequestDto(hours)).toDomain()
    }

    override suspend fun updateTask(
        id: Long, diagnostic: String?, previewHours: Float?, notes: String?
    ): Result<WorkshopTask> = runCatching {
        // El backend espera el objeto completo (PUT), pero solo enviamos
        // los campos que han cambiado. Los campos null los ignora el servidor
        // gracias a NullValuePropertyMappingStrategy.IGNORE del mapper MapStruct.
        val current = taskApiService.getTaskById(id)
        taskApiService.updateTask(
            id      = id,
            request = WorkshopTaskRequestDto(
                vehicleId    = current.vehicleId,
                mechanicId   = current.mechanicId,
                diagnostic   = diagnostic   ?: current.diagnostic,
                previewHours = previewHours ?: current.previewHours,
                initDate     = current.initDate,
                notes        = notes        ?: current.notes
            )
        ).toDomain()
    }

    override suspend fun finishTask(id: Long, solution: String?): Result<WorkshopTask> =
        runCatching {
            taskApiService.finishTask(id, solution).toDomain()
        }

    override suspend fun markAsPaid(id: Long): Result<WorkshopTask> = runCatching {
        taskApiService.markAsPaid(id).toDomain()
    }

    override suspend fun deleteTask(id: Long): Result<Unit> = runCatching {
        taskApiService.deleteTask(id)
    }

    private fun pagingConfig() = PagingConfig(
        pageSize = PAGE_SIZE, enablePlaceholders = false, prefetchDistance = 5
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ReportRepositoryImpl
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Implementación concreta de [ReportRepository].
 */
class ReportRepositoryImpl @Inject constructor(
    private val reportApiService: ReportApiService
) : ReportRepository {

    override suspend fun getSummaryReport(): Result<SummaryReport> = runCatching {
        reportApiService.getSummaryReport().toDomain()
    }
}
