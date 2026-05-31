package com.hotguy.workshopmanagement.data.remote.dto.auth

import com.hotguy.workshopmanagement.domain.model.AuthToken
import com.hotguy.workshopmanagement.domain.model.UserRole

/**
 * Convierte el [AuthResponseDto] de red al modelo de dominio [AuthToken].
 *
 * La conversión del rol merece atención: el backend devuelve el rol con el
 * prefijo "ROLE_" que añade Spring Security (ej. "ROLE_ADMIN"). El dominio
 * usa el enum [UserRole] sin ese prefijo. [UserRole.fromJwtClaim] se encarga
 * de la normalización.
 *
 * Si el rol no se reconoce (valor inesperado del servidor), se usa CLIENT
 * como fallback seguro — el usuario tendrá el acceso más restringido posible
 * en lugar de obtener permisos incorrectos.
 */
fun AuthResponseDto.toDomain(): AuthToken = AuthToken(
    accessToken  = accessToken,
    refreshToken = refreshToken,
    role         = UserRole.fromJwtClaim(role) ?: UserRole.CLIENT
)
