package com.hotguy.workshopmanagement.domain.model

/**
 * Modelo de dominio que representa el par de tokens JWT tras una autenticación exitosa.
 *
 * Este modelo vive en la capa de dominio y es lo que manejan los Use Cases y
 * ViewModels. Es distinto del [com.hotguy.workshopmanagement.data.remote.dto.auth.AuthResponseDto],
 * que es lo que devuelve literalmente la API. La capa de datos transforma uno en
 * otro mediante funciones mapper, manteniendo el dominio desacoplado de Retrofit.
 *
 * @param accessToken  Token JWT de corta duración (1 hora) para autenticar peticiones.
 * @param refreshToken Token opaco de larga duración (7 días) para renovar el access token.
 * @param role         Rol del usuario extraído del claim del JWT, ya convertido a [UserRole].
 */
data class AuthToken(
    val accessToken:  String,
    val refreshToken: String,
    val role:         UserRole
)
