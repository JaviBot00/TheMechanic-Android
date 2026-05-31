package com.hotguy.workshopmanagement.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wrapper genérico que mapea la respuesta paginada de Spring Data.
 *
 * Cuando el backend devuelve una [Page<T>], el JSON tiene esta estructura:
 * {
 *   "content": [...],
 *   "totalElements": 42,
 *   "totalPages": 3,
 *   "number": 0,        ← número de página actual (0-indexed)
 *   "size": 20,
 *   "last": false
 * }
 *
 * Esta clase se usa en los [PagingSource] de cada dominio para deserializar
 * la respuesta paginada y extraer el contenido y los metadatos de paginación.
 *
 * @param T El tipo del DTO contenido en la lista (ej. [ClientDto], [VehicleDto]).
 */
@Serializable
data class PagedResponseDto<T>(
    @SerialName("content")       val content:       List<T>,
    @SerialName("totalElements") val totalElements: Long,
    @SerialName("totalPages")    val totalPages:    Int,
    @SerialName("number")        val number:        Int,      // página actual (0-indexed)
    @SerialName("size")          val size:          Int,
    @SerialName("last")          val last:          Boolean
)
