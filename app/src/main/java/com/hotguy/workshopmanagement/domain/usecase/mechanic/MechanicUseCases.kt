package com.hotguy.workshopmanagement.domain.usecase.mechanic

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.Mechanic
import com.hotguy.workshopmanagement.domain.repository.MechanicRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use Case: obtener la lista paginada de todos los mecánicos activos.
 */
class GetMechanicsUseCase @Inject constructor(
    private val mechanicRepository: MechanicRepository
) {
    operator fun invoke(): Flow<PagingData<Mechanic>> = mechanicRepository.getMechanics()
}

/**
 * Use Case: buscar mecánicos por especialidad.
 *
 * @param specialty Texto a buscar. Si está vacío, devuelve todos los mecánicos.
 */
class SearchMechanicsBySpecialtyUseCase @Inject constructor(
    private val mechanicRepository: MechanicRepository
) {
    operator fun invoke(specialty: String): Flow<PagingData<Mechanic>> =
        if (specialty.isBlank()) mechanicRepository.getMechanics()
        else mechanicRepository.getMechanicsBySpecialty(specialty.trim())
}

/**
 * Use Case: obtener el detalle de un mecánico por su ID.
 */
class GetMechanicByIdUseCase @Inject constructor(
    private val mechanicRepository: MechanicRepository
) {
    suspend operator fun invoke(id: Long): Result<Mechanic> =
        mechanicRepository.getMechanicById(id)
}

/**
 * Use Case: registrar un nuevo mecánico en el sistema.
 *
 * Reglas de dominio:
 * - Nombre, apellido, NIF, email y especialidad son obligatorios.
 * - La fecha de registro no puede ser futura (el backend también lo valida
 *   con @PastOrPresent, pero lo comprobamos aquí para feedback inmediato).
 */
class CreateMechanicUseCase @Inject constructor(
    private val mechanicRepository: MechanicRepository
) {
    suspend operator fun invoke(
        name:             String,
        surname1:         String,
        surname2:         String?,
        nif:              String,
        email:            String,
        telephone:        String?,
        registrationDate: LocalDate,
        specialty:        String
    ): Result<Mechanic> {
        if (name.isBlank())      return Result.failure(IllegalArgumentException("El nombre es obligatorio"))
        if (surname1.isBlank())  return Result.failure(IllegalArgumentException("El primer apellido es obligatorio"))
        if (nif.isBlank())       return Result.failure(IllegalArgumentException("El NIF es obligatorio"))
        if (email.isBlank())     return Result.failure(IllegalArgumentException("El email es obligatorio"))
        if (specialty.isBlank()) return Result.failure(IllegalArgumentException("La especialidad es obligatoria"))
        if (registrationDate.isAfter(LocalDate.now())) return Result.failure(
            IllegalArgumentException("La fecha de registro no puede ser futura")
        )

        return mechanicRepository.createMechanic(
            name.trim(), surname1.trim(), surname2?.trim(),
            nif.trim().uppercase(), email.trim(), telephone?.trim(),
            registrationDate, specialty.trim()
        )
    }
}

/**
 * Use Case: actualizar los datos de un mecánico existente.
 */
class UpdateMechanicUseCase @Inject constructor(
    private val mechanicRepository: MechanicRepository
) {
    suspend operator fun invoke(
        id:               Long,
        name:             String,
        surname1:         String,
        surname2:         String?,
        nif:              String,
        email:            String,
        telephone:        String?,
        registrationDate: LocalDate,
        specialty:        String
    ): Result<Mechanic> {
        if (name.isBlank())      return Result.failure(IllegalArgumentException("El nombre es obligatorio"))
        if (surname1.isBlank())  return Result.failure(IllegalArgumentException("El primer apellido es obligatorio"))
        if (nif.isBlank())       return Result.failure(IllegalArgumentException("El NIF es obligatorio"))
        if (email.isBlank())     return Result.failure(IllegalArgumentException("El email es obligatorio"))
        if (specialty.isBlank()) return Result.failure(IllegalArgumentException("La especialidad es obligatoria"))

        return mechanicRepository.updateMechanic(
            id, name.trim(), surname1.trim(), surname2?.trim(),
            nif.trim().uppercase(), email.trim(), telephone?.trim(),
            registrationDate, specialty.trim()
        )
    }
}

/**
 * Use Case: eliminar lógicamente un mecánico.
 * El servidor rechaza la operación si el mecánico tiene tareas activas.
 */
class DeleteMechanicUseCase @Inject constructor(
    private val mechanicRepository: MechanicRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = mechanicRepository.deleteMechanic(id)
}
