package com.hotguy.workshopmanagement.data.remote.api

import com.hotguy.workshopmanagement.data.remote.dto.client.ClientDto
import com.hotguy.workshopmanagement.data.remote.dto.client.ClientRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.common.PagedResponseDto
import com.hotguy.workshopmanagement.data.remote.dto.mechanic.MechanicDto
import com.hotguy.workshopmanagement.data.remote.dto.mechanic.MechanicRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.report.SummaryReportDto
import com.hotguy.workshopmanagement.data.remote.dto.task.AddHoursRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.task.WorkshopTaskDto
import com.hotguy.workshopmanagement.data.remote.dto.task.WorkshopTaskRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.vehicle.VehicleDto
import com.hotguy.workshopmanagement.data.remote.dto.vehicle.VehicleRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// ─────────────────────────────────────────────────────────────────────────────
// ClientApiService
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Interfaz Retrofit para los endpoints de clientes.
 *
 * Los parámetros de paginación [page] y [size] se corresponden con los
 * query params que Spring Data Pageable espera: ?page=0&size=20&sort=surname1,asc
 * El parámetro [sort] también se puede pasar si queremos ordenación dinámica.
 */
interface ClientApiService {

    @GET("api/v1/clients")
    suspend fun getClients(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): PagedResponseDto<ClientDto>

    @GET("api/v1/clients/search")
    suspend fun searchBySurname(
        @Query("surname1") surname1: String,
        @Query("page")     page:     Int,
        @Query("size")     size:     Int = 20
    ): PagedResponseDto<ClientDto>

    @GET("api/v1/clients/{id}")
    suspend fun getClientById(@Path("id") id: Long): ClientDto

    @GET("api/v1/clients/by-nif/{nif}")
    suspend fun getClientByNif(@Path("nif") nif: String): ClientDto

    @POST("api/v1/clients")
    suspend fun createClient(@Body request: ClientRequestDto): ClientDto

    @PUT("api/v1/clients/{id}")
    suspend fun updateClient(
        @Path("id")   id:      Long,
        @Body         request: ClientRequestDto
    ): ClientDto

    @DELETE("api/v1/clients/{id}")
    suspend fun deleteClient(@Path("id") id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// VehicleApiService
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Interfaz Retrofit para los endpoints de vehículos.
 */
interface VehicleApiService {

    @GET("api/v1/vehicles")
    suspend fun getVehicles(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): PagedResponseDto<VehicleDto>

    @GET("api/v1/vehicles/by-client/{clientId}")
    suspend fun getVehiclesByClient(
        @Path("clientId") clientId: Long,
        @Query("page")    page:     Int,
        @Query("size")    size:     Int = 20
    ): PagedResponseDto<VehicleDto>

    @GET("api/v1/vehicles/by-type")
    suspend fun getVehiclesByType(
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): PagedResponseDto<VehicleDto>

    @GET("api/v1/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Long): VehicleDto

    @POST("api/v1/vehicles")
    suspend fun createVehicle(@Body request: VehicleRequestDto): VehicleDto

    @PUT("api/v1/vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") id:      Long,
        @Body       request: VehicleRequestDto
    ): VehicleDto

    @DELETE("api/v1/vehicles/{id}")
    suspend fun deleteVehicle(@Path("id") id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// MechanicApiService
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Interfaz Retrofit para los endpoints de mecánicos.
 */
interface MechanicApiService {

    @GET("api/v1/mechanics")
    suspend fun getMechanics(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): PagedResponseDto<MechanicDto>

    @GET("api/v1/mechanics/search")
    suspend fun searchBySpecialty(
        @Query("specialty") specialty: String,
        @Query("page")      page:      Int,
        @Query("size")      size:      Int = 20
    ): PagedResponseDto<MechanicDto>

    @GET("api/v1/mechanics/{id}")
    suspend fun getMechanicById(@Path("id") id: Long): MechanicDto

    @POST("api/v1/mechanics")
    suspend fun createMechanic(@Body request: MechanicRequestDto): MechanicDto

    @PUT("api/v1/mechanics/{id}")
    suspend fun updateMechanic(
        @Path("id") id:      Long,
        @Body       request: MechanicRequestDto
    ): MechanicDto

    @DELETE("api/v1/mechanics/{id}")
    suspend fun deleteMechanic(@Path("id") id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// WorkshopTaskApiService
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Interfaz Retrofit para los endpoints de tareas de taller.
 *
 * Los endpoints de acción sobre el estado de la tarea (añadir horas, finalizar,
 * marcar como pagada) usan @PATCH porque modifican parcialmente el recurso,
 * no lo reemplazan completo.
 */
interface WorkshopTaskApiService {

    @GET("api/v1/tasks")
    suspend fun getTasks(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): PagedResponseDto<WorkshopTaskDto>

    @GET("api/v1/tasks/pending")
    suspend fun getPendingTasks(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): PagedResponseDto<WorkshopTaskDto>

    @GET("api/v1/tasks/unpaid")
    suspend fun getUnpaidTasks(
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): PagedResponseDto<WorkshopTaskDto>

    @GET("api/v1/tasks/by-client/{clientId}")
    suspend fun getTasksByClient(
        @Path("clientId") clientId: Long,
        @Query("page")    page:     Int,
        @Query("size")    size:     Int = 20
    ): PagedResponseDto<WorkshopTaskDto>

    @GET("api/v1/tasks/by-vehicle/{vehicleId}")
    suspend fun getTasksByVehicle(
        @Path("vehicleId") vehicleId: Long,
        @Query("page")     page:      Int,
        @Query("size")     size:      Int = 20
    ): PagedResponseDto<WorkshopTaskDto>

    @GET("api/v1/tasks/by-mechanic/{mechanicId}")
    suspend fun getTasksByMechanic(
        @Path("mechanicId") mechanicId: Long,
        @Query("page")      page:       Int,
        @Query("size")      size:       Int = 20
    ): PagedResponseDto<WorkshopTaskDto>

    @GET("api/v1/tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Long): WorkshopTaskDto

    @POST("api/v1/tasks")
    suspend fun createTask(@Body request: WorkshopTaskRequestDto): WorkshopTaskDto

    @PUT("api/v1/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id:      Long,
        @Body       request: WorkshopTaskRequestDto
    ): WorkshopTaskDto

    @PATCH("api/v1/tasks/{id}/hours")
    suspend fun addHours(
        @Path("id") id:      Long,
        @Body       request: AddHoursRequestDto
    ): WorkshopTaskDto

    @PATCH("api/v1/tasks/{id}/finish")
    suspend fun finishTask(
        @Path("id")       id:       Long,
        @Query("solution") solution: String? = null
    ): WorkshopTaskDto

    @PATCH("api/v1/tasks/{id}/pay")
    suspend fun markAsPaid(@Path("id") id: Long): WorkshopTaskDto

    @DELETE("api/v1/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// ReportApiService
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Interfaz Retrofit para los endpoints de reportes.
 */
interface ReportApiService {

    @GET("api/v1/reports/summary")
    suspend fun getSummaryReport(): SummaryReportDto
}
