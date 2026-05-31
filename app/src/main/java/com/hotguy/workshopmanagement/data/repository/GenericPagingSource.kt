package com.hotguy.workshopmanagement.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * [PagingSource] genérico y reutilizable para cualquier endpoint paginado del backend.
 *
 * Paging 3 necesita un [PagingSource] por cada fuente de datos paginada. Sin este
 * helper genérico, tendríamos que escribir la misma lógica de carga de páginas en
 * cada repositorio (clientes, vehículos, mecánicos, tareas). Con este helper, cada
 * repositorio solo necesita pasar una lambda que llama al endpoint correcto.
 *
 * El backend usa paginación 0-indexed (la primera página es la número 0), que es
 * la convención de Spring Data. Paging 3 también empieza en 0 por defecto, por lo
 * que no hay conversión de índices necesaria.
 *
 * @param T         Tipo del ítem de dominio que contiene la lista resultante.
 * @param fetchPage Lambda que recibe el número de página y devuelve una lista de ítems.
 *                  Debe lanzar una excepción si la petición de red falla — Paging 3
 *                  lo captura y lo convierte en un estado de error automáticamente.
 */
class GenericPagingSource<T : Any>(
    private val fetchPage: suspend (page: Int) -> List<T>
) : PagingSource<Int, T>() {

    /**
     * Carga una página de datos.
     *
     * Paging 3 llama a este método cada vez que necesita más datos:
     * al cargar la primera página, al hacer scroll y al reintentar tras un error.
     *
     * @param params Contiene la clave de la página a cargar ([params.key]) y el
     *               tamaño de página solicitado ([params.loadSize]). Usamos [params.key]
     *               como número de página; si es null, cargamos la primera (página 0).
     * @return [LoadResult.Page] con los datos y las claves de la página anterior/siguiente,
     *         o [LoadResult.Error] si la petición falla.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 0   // null = primera carga → empezar en la página 0

        return runCatching {
            val items = fetchPage(page)
            LoadResult.Page(
                data         = items,
                // Si estamos en la primera página, no hay página anterior
                prevKey      = if (page == 0) null else page - 1,
                // Si la respuesta viene vacía, no hay más páginas
                nextKey      = if (items.isEmpty()) null else page + 1
            )
        }.getOrElse { exception ->
            LoadResult.Error(exception)
        }
    }

    /**
     * Devuelve la clave de la página de refresco más cercana al ítem actualmente
     * visible cuando Paging 3 invalida y recarga la fuente de datos.
     *
     * La implementación más simple es devolver la clave del ancla si existe,
     * o null para recargar desde el principio. Para una app de gestión sin
     * scroll infinito crítico, esto es suficiente.
     */
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
