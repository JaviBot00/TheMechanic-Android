package com.hotguy.workshopmanagement.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de respuesta para los endpoints de login y refresh.
 *
 * Mapea exactamente el JSON que devuelve el backend:
 * {
 *   "accessToken":  "eyJhbGci...",
 *   "refreshToken": "550e8400-...",
 *   "tokenType":    "Bearer",
 *   "role":         "ROLE_ADMIN"
 * }
 *
 * [@Serializable] activa el procesador de kotlinx-serialization para generar
 * el código de serialización/deserialización en tiempo de compilación.
 * Es más rápido que la reflection de Gson porque no hay introspección en runtime.
 *
 * [@SerialName] mapea el campo JSON (camelCase del backend) al nombre de la
 * propiedad Kotlin. Aquí coinciden, pero lo dejamos explícito para que sea
 * obvio qué nombre viene del servidor.
 */
@Serializable
data class AuthResponseDto(
    @SerialName("accessToken")  val accessToken:  String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("tokenType")    val tokenType:    String,
    @SerialName("role")         val role:         String
)

/**
 * DTO de petición para el endpoint de login.
 *
 * El backend espera:
 * { "username": "admin", "password": "secret123" }
 */
@Serializable
data class LoginRequestDto(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

/**
 * DTO de petición para el endpoint de refresco de token.
 *
 * El backend espera:
 * { "refreshToken": "550e8400-e29b-..." }
 */
@Serializable
data class RefreshRequestDto(
    @SerialName("refreshToken") val refreshToken: String
)
