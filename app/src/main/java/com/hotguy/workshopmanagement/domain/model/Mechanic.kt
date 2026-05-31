package com.hotguy.workshopmanagement.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Modelo de dominio que representa a un mecánico del taller.
 *
 * @param id               Identificador técnico interno.
 * @param name             Nombre de pila.
 * @param surname1         Primer apellido.
 * @param surname2         Segundo apellido (puede ser null).
 * @param nif              NIF — identificador único de negocio.
 * @param email            Correo electrónico de contacto.
 * @param telephone        Teléfono de contacto (puede ser null).
 * @param registrationDate Fecha de incorporación al taller.
 * @param specialty        Especialidad principal (ej. "Electricidad", "Chapa y pintura").
 * @param taskCount        Número de tareas actualmente asignadas al mecánico.
 * @param createdAt        Fecha de alta en el sistema.
 * @param updatedAt        Fecha de la última modificación.
 */
data class Mechanic(
    val id:               Long,
    val name:             String,
    val surname1:         String,
    val surname2:         String?,
    val nif:              String,
    val email:            String,
    val telephone:        String?,
    val registrationDate: LocalDate,
    val specialty:        String,
    val taskCount:        Int,
    val createdAt:        Instant,
    val updatedAt:        Instant
) {
    /**
     * Nombre completo del mecánico en formato "Nombre Apellido1 Apellido2".
     */
    val fullName: String
        get() = listOfNotNull(name, surname1, surname2).joinToString(" ")
}
