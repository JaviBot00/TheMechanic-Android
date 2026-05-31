package com.hotguy.workshopmanagement.domain.model

import java.time.Instant

/**
 * Tipos de vehículo soportados por el sistema.
 *
 * Se define aquí en el dominio porque las reglas de negocio (por ejemplo,
 * mostrar la tarifa correspondiente en la UI) dependen del tipo. El valor
 * de cadena [apiValue] es el que devuelve el backend en el JSON y el que
 * se usa para deserializar correctamente.
 *
 * @param apiValue  Nombre tal como lo devuelve el backend ("CAR", "VAN"...).
 * @param labelEs   Nombre en español para mostrar en la UI.
 * @param hourlyRate Tarifa horaria en euros.
 * @param fixedFee   Cargo fijo adicional en euros (por dificultad del vehículo).
 */
enum class VehicleType(
    val apiValue:   String,
    val labelEs:    String,
    val hourlyRate: Float,
    val fixedFee:   Float
) {
    MOTORCYCLE("MOTORCYCLE", "Motocicleta", 20f, 0f),
    CAR("CAR",               "Coche",       25f, 0f),
    VAN("VAN",               "Furgoneta",   30f, 30f),
    TRUCK("TRUCK",           "Camión",      40f, 50f);

    companion object {
        /**
         * Convierte el string que devuelve el backend al enum correspondiente.
         * Si el valor no se reconoce devuelve [CAR] como fallback seguro.
         */
        fun fromApi(value: String): VehicleType =
            entries.firstOrNull { it.apiValue == value } ?: CAR
    }
}

/**
 * Modelo de dominio que representa un vehículo registrado en el taller.
 *
 * @param id               Identificador técnico interno.
 * @param registrationCode Matrícula del vehículo — identificador único de negocio.
 * @param model            Marca y modelo (ej. "Toyota Corolla").
 * @param type             Tipo de vehículo, que determina las tarifas de facturación.
 * @param clientId         Identificador del propietario.
 * @param clientName       Nombre completo del propietario (incluido para evitar joins en UI).
 * @param taskCount        Número total de tareas asociadas al vehículo.
 * @param completionPct    Porcentaje de tareas completadas (0–100).
 * @param totalRevenue     Facturación total generada por este vehículo (tareas pagadas).
 * @param createdAt        Fecha de alta en el sistema.
 * @param updatedAt        Fecha de la última modificación.
 */
data class Vehicle(
    val id:               Long,
    val registrationCode: String,
    val model:            String,
    val type:             VehicleType,
    val clientId:         Long,
    val clientName:       String,
    val taskCount:        Int,
    val completionPct:    Float,
    val totalRevenue:     Float,
    val createdAt:        Instant,
    val updatedAt:        Instant
)
