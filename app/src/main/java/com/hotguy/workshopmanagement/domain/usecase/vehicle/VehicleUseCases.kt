package com.hotguy.workshopmanagement.domain.usecase.vehicle

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.domain.model.VehicleType
import com.hotguy.workshopmanagement.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case: obtener la lista paginada de todos los vehículos.
 */
class GetVehiclesUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(): Flow<PagingData<Vehicle>> = vehicleRepository.getVehicles()
}

/**
 * Use Case: obtener los vehículos de un cliente concreto.
 * Lo usan tanto ADMIN/MECHANIC (desde el detalle del cliente) como CLIENT
 * (para ver solo sus propios vehículos).
 *
 * @param clientId Identificador del propietario.
 */
class GetVehiclesByClientUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(clientId: Long): Flow<PagingData<Vehicle>> =
        vehicleRepository.getVehiclesByClient(clientId)
}

/**
 * Use Case: obtener los vehículos filtrados por tipo.
 *
 * @param type Tipo de vehículo a filtrar.
 */
class GetVehiclesByTypeUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(type: VehicleType): Flow<PagingData<Vehicle>> =
        vehicleRepository.getVehiclesByType(type)
}

/**
 * Use Case: obtener el detalle de un vehículo por su ID.
 */
class GetVehicleByIdUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(id: Long): Result<Vehicle> = vehicleRepository.getVehicleById(id)
}

/**
 * Use Case: registrar un nuevo vehículo y vincularlo a su propietario.
 *
 * Reglas de dominio:
 * - La matrícula no puede estar vacía.
 * - El modelo no puede estar vacío.
 * - El clientId debe ser un ID válido (mayor que 0).
 */
class CreateVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(
        registrationCode: String,
        model:            String,
        type:             VehicleType,
        clientId:         Long
    ): Result<Vehicle> {
        if (registrationCode.isBlank()) return Result.failure(
            IllegalArgumentException("La matrícula es obligatoria")
        )
        if (model.isBlank()) return Result.failure(
            IllegalArgumentException("El modelo es obligatorio")
        )
        if (clientId <= 0) return Result.failure(
            IllegalArgumentException("Debe seleccionar un propietario válido")
        )

        return vehicleRepository.createVehicle(
            registrationCode = registrationCode.trim().uppercase(),
            model            = model.trim(),
            type             = type,
            clientId         = clientId
        )
    }
}

/**
 * Use Case: actualizar los datos de un vehículo existente.
 */
class UpdateVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(
        id:               Long,
        registrationCode: String,
        model:            String,
        type:             VehicleType,
        clientId:         Long
    ): Result<Vehicle> {
        if (registrationCode.isBlank()) return Result.failure(
            IllegalArgumentException("La matrícula es obligatoria")
        )
        if (model.isBlank()) return Result.failure(
            IllegalArgumentException("El modelo es obligatorio")
        )

        return vehicleRepository.updateVehicle(
            id               = id,
            registrationCode = registrationCode.trim().uppercase(),
            model            = model.trim(),
            type             = type,
            clientId         = clientId
        )
    }
}

/**
 * Use Case: eliminar lógicamente un vehículo.
 * El servidor rechaza la operación si hay tareas activas asignadas.
 */
class DeleteVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = vehicleRepository.deleteVehicle(id)
}
