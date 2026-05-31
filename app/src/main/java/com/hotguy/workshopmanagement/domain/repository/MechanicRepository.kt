package com.hotguy.workshopmanagement.domain.repository

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.Mechanic
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Contrato del repositorio de mecánicos.
 */
interface MechanicRepository {

    /**
     * Stream paginado de todos los mecánicos activos, ordenados por apellido.
     */
    fun getMechanics(): Flow<PagingData<Mechanic>>

    /**
     * Stream paginado de mecánicos filtrado por especialidad.
     *
     * @param specialty Texto a buscar en el campo de especialidad.
     */
    fun getMechanicsBySpecialty(specialty: String): Flow<PagingData<Mechanic>>

    /**
     * Obtiene un mecánico por su identificador técnico.
     *
     * @param id Identificador del mecánico.
     * @return [Result] con el [Mechanic] o con la excepción si no existe.
     */
    suspend fun getMechanicById(id: Long): Result<Mechanic>

    /**
     * Registra un nuevo mecánico en el sistema. Solo ADMIN.
     *
     * @param name             Nombre de pila.
     * @param surname1         Primer apellido.
     * @param surname2         Segundo apellido (opcional).
     * @param nif              NIF en formato "12345678A".
     * @param email            Correo electrónico.
     * @param telephone        Teléfono de contacto (opcional).
     * @param registrationDate Fecha de incorporación al taller.
     * @param specialty        Especialidad principal.
     * @return [Result] con el [Mechanic] creado.
     */
    suspend fun createMechanic(
        name:             String,
        surname1:         String,
        surname2:         String?,
        nif:              String,
        email:            String,
        telephone:        String?,
        registrationDate: LocalDate,
        specialty:        String
    ): Result<Mechanic>

    /**
     * Actualiza los datos de un mecánico. Solo ADMIN.
     *
     * @param id El identificador del mecánico a actualizar.
     * Los demás parámetros son los mismos que en [createMechanic].
     * @return [Result] con el [Mechanic] actualizado.
     */
    suspend fun updateMechanic(
        id:               Long,
        name:             String,
        surname1:         String,
        surname2:         String?,
        nif:              String,
        email:            String,
        telephone:        String?,
        registrationDate: LocalDate,
        specialty:        String
    ): Result<Mechanic>

    /**
     * Elimina lógicamente un mecánico. Solo ADMIN.
     * Falla si el mecánico tiene tareas activas asignadas.
     *
     * @param id Identificador del mecánico.
     * @return [Result.success] si se eliminó, [Result.failure] si tiene tareas activas.
     */
    suspend fun deleteMechanic(id: Long): Result<Unit>
}
