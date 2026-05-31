package com.hotguy.workshopmanagement.domain.repository

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.domain.model.VehicleType
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de vehículos.
 */
interface VehicleRepository {

    /**
     * Stream paginado de todos los vehículos activos, ordenados por matrícula.
     */
    fun getVehicles(): Flow<PagingData<Vehicle>>

    /**
     * Stream paginado de los vehículos pertenecientes a un cliente concreto.
     * Usado tanto por ADMIN/MECHANIC (al ver el detalle de un cliente) como
     * por CLIENT (que solo ve los suyos).
     *
     * @param clientId Identificador del propietario.
     */
    fun getVehiclesByClient(clientId: Long): Flow<PagingData<Vehicle>>

    /**
     * Stream paginado de vehículos filtrado por tipo.
     *
     * @param type Tipo de vehículo (CAR, VAN, etc.).
     */
    fun getVehiclesByType(type: VehicleType): Flow<PagingData<Vehicle>>

    /**
     * Obtiene un vehículo por su identificador técnico.
     *
     * @param id Identificador del vehículo.
     * @return [Result] con el [Vehicle] o con la excepción si no existe.
     */
    suspend fun getVehicleById(id: Long): Result<Vehicle>

    /**
     * Registra un nuevo vehículo y lo vincula a su propietario.
     *
     * @param registrationCode Matrícula del vehículo.
     * @param model            Marca y modelo (ej. "Toyota Corolla").
     * @param type             Tipo de vehículo.
     * @param clientId         Identificador del propietario.
     * @return [Result] con el [Vehicle] creado.
     */
    suspend fun createVehicle(
        registrationCode: String,
        model:            String,
        type:             VehicleType,
        clientId:         Long
    ): Result<Vehicle>

    /**
     * Actualiza los datos de un vehículo existente.
     *
     * @param id               Identificador del vehículo a actualizar.
     * @param registrationCode Nueva matrícula.
     * @param model            Nuevo modelo.
     * @param type             Nuevo tipo.
     * @param clientId         Nuevo propietario (permite cambiar el titular).
     * @return [Result] con el [Vehicle] actualizado.
     */
    suspend fun updateVehicle(
        id:               Long,
        registrationCode: String,
        model:            String,
        type:             VehicleType,
        clientId:         Long
    ): Result<Vehicle>

    /**
     * Elimina lógicamente un vehículo.
     * Falla si el vehículo tiene tareas activas asignadas.
     *
     * @param id Identificador del vehículo.
     * @return [Result.success] si se eliminó, [Result.failure] si tiene tareas activas.
     */
    suspend fun deleteVehicle(id: Long): Result<Unit>
}
