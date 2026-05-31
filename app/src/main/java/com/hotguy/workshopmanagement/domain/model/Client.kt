package com.hotguy.workshopmanagement.domain.model

import java.time.Instant

/**
 * Modelo de dominio que representa a un cliente del taller.
 *
 * Contiene únicamente los campos que la lógica de negocio y la UI necesitan.
 * No incluye referencias circulares (la lista completa de vehículos no está
 * aquí; se carga bajo demanda desde [VehicleRepository]).
 *
 * @param id           Identificador técnico interno generado por la base de datos.
 * @param clientCode   Código de negocio asignado por el taller (distinto del id).
 * @param name         Nombre de pila del cliente.
 * @param surname1     Primer apellido.
 * @param surname2     Segundo apellido (puede ser null).
 * @param nif          Número de Identificación Fiscal — identificador único de negocio.
 * @param email        Correo electrónico de contacto.
 * @param telephone    Teléfono de contacto (puede ser null).
 * @param vehicleCount Número de vehículos activos asociados a este cliente.
 * @param createdAt    Fecha y hora de alta en el sistema.
 * @param updatedAt    Fecha y hora de la última modificación.
 */
data class Client(
    val id:           Long,
    val clientCode:   Int,
    val name:         String,
    val surname1:     String,
    val surname2:     String?,
    val nif:          String,
    val email:        String,
    val telephone:    String?,
    val vehicleCount: Int,
    val createdAt:    Instant,
    val updatedAt:    Instant
) {
    /**
     * Nombre completo del cliente en formato "Nombre Apellido1 Apellido2".
     * Usado en labels y cabeceras de pantalla.
     */
    val fullName: String
        get() = listOfNotNull(name, surname1, surname2).joinToString(" ")
}
