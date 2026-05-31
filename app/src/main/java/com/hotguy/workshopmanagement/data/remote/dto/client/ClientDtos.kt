package com.hotguy.workshopmanagement.data.remote.dto.client

import com.hotguy.workshopmanagement.domain.model.Client
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * DTO de respuesta que mapea exactamente el JSON del backend para un cliente.
 *
 * Refleja el record [ClientResponse] del servidor. Los campos en camelCase
 * coinciden con los nombres del JSON, por lo que @SerialName es redundante
 * aquí pero lo dejamos explícito como documentación del contrato de la API.
 */
@Serializable
data class ClientDto(
    @SerialName("id")           val id:           Long,
    @SerialName("clientCode")   val clientCode:   Int,
    @SerialName("name")         val name:         String,
    @SerialName("surname1")     val surname1:     String,
    @SerialName("surname2")     val surname2:     String? = null,
    @SerialName("nif")          val nif:          String,
    @SerialName("email")        val email:        String,
    @SerialName("telephone")    val telephone:    String? = null,
    @SerialName("vehicleCount") val vehicleCount: Int,
    @SerialName("createdAt")    val createdAt:    String,   // ISO-8601, ej. "2026-01-15T10:30:00Z"
    @SerialName("updatedAt")    val updatedAt:    String
)

/**
 * DTO de petición para crear o actualizar un cliente.
 *
 * Mapea el record [ClientRequest] del servidor.
 */
@Serializable
data class ClientRequestDto(
    @SerialName("clientCode") val clientCode: Int,
    @SerialName("name")       val name:       String,
    @SerialName("surname1")   val surname1:   String,
    @SerialName("surname2")   val surname2:   String? = null,
    @SerialName("nif")        val nif:        String,
    @SerialName("email")      val email:      String,
    @SerialName("telephone")  val telephone:  String? = null
)

// ── Mapper DTO → Dominio ──────────────────────────────────────────────────────

/**
 * Convierte un [ClientDto] de red al modelo de dominio [Client].
 *
 * Esta función de extensión vive en la capa de datos — conoce tanto el DTO
 * como el modelo de dominio. El dominio nunca importa nada de este paquete.
 *
 * Los campos de fecha vienen como String ISO-8601 desde el backend y se
 * convierten a [Instant] aquí, en el límite entre capas.
 */
fun ClientDto.toDomain(): Client = Client(
    id           = id,
    clientCode   = clientCode,
    name         = name,
    surname1     = surname1,
    surname2     = surname2,
    nif          = nif,
    email        = email,
    telephone    = telephone,
    vehicleCount = vehicleCount,
    createdAt    = Instant.parse(createdAt),
    updatedAt    = Instant.parse(updatedAt)
)
