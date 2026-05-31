package com.hotguy.workshopmanagement.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hotguy.workshopmanagement.data.remote.api.ClientApiService
import com.hotguy.workshopmanagement.data.remote.dto.client.ClientRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.client.toDomain
import com.hotguy.workshopmanagement.domain.model.Client
import com.hotguy.workshopmanagement.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación concreta de [ClientRepository].
 *
 * Los métodos de listado usan Paging 3 para cargar los datos en páginas
 * de forma incremental. La clase [Pager] de Paging 3 construye el Flow
 * a partir de un [GenericPagingSource] que llama al endpoint con el número
 * de página correcto.
 *
 * [PAGE_SIZE] debe coincidir con el tamaño de página por defecto del backend
 * (20 en este caso, definido con @PageableDefault en el Controller). Si se
 * cambia en el servidor hay que actualizarlo aquí también.
 */
class ClientRepositoryImpl @Inject constructor(
    private val clientApiService: ClientApiService
) : ClientRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    // ── Listados paginados ────────────────────────────────────────────────────

    override fun getClients(): Flow<PagingData<Client>> = Pager(
        config = pagingConfig()
    ) {
        GenericPagingSource { page ->
            clientApiService.getClients(page = page, size = PAGE_SIZE)
                .content
                .map { it.toDomain() }
        }
    }.flow

    override fun searchClientsBySurname(query: String): Flow<PagingData<Client>> = Pager(
        config = pagingConfig()
    ) {
        GenericPagingSource { page ->
            clientApiService.searchBySurname(surname1 = query, page = page, size = PAGE_SIZE)
                .content
                .map { it.toDomain() }
        }
    }.flow

    // ── Operaciones puntuales ─────────────────────────────────────────────────

    override suspend fun getClientById(id: Long): Result<Client> = runCatching {
        clientApiService.getClientById(id).toDomain()
    }

    override suspend fun getClientByNif(nif: String): Result<Client> = runCatching {
        clientApiService.getClientByNif(nif).toDomain()
    }

    override suspend fun createClient(
        clientCode: Int,
        name:       String,
        surname1:   String,
        surname2:   String?,
        nif:        String,
        email:      String,
        telephone:  String?
    ): Result<Client> = runCatching {
        clientApiService.createClient(
            ClientRequestDto(clientCode, name, surname1, surname2, nif, email, telephone)
        ).toDomain()
    }

    override suspend fun updateClient(
        id:         Long,
        clientCode: Int,
        name:       String,
        surname1:   String,
        surname2:   String?,
        nif:        String,
        email:      String,
        telephone:  String?
    ): Result<Client> = runCatching {
        clientApiService.updateClient(
            id      = id,
            request = ClientRequestDto(clientCode, name, surname1, surname2, nif, email, telephone)
        ).toDomain()
    }

    override suspend fun deleteClient(id: Long): Result<Unit> = runCatching {
        clientApiService.deleteClient(id)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Configuración de paginación compartida por todos los listados.
     *
     * - [pageSize]: cuántos ítems se cargan en cada petición.
     * - [enablePlaceholders]: false → la lista no reserva espacio para ítems
     *   no cargados aún. Simplifica el renderizado en Compose (no hay que
     *   manejar null items en el LazyColumn).
     * - [prefetchDistance]: Paging 3 precarga la siguiente página cuando
     *   el usuario está a [prefetchDistance] ítems del final de la lista.
     *   Con 5, la carga es bastante fluida sin ser demasiado agresiva.
     */
    private fun pagingConfig() = PagingConfig(
        pageSize          = PAGE_SIZE,
        enablePlaceholders = false,
        prefetchDistance   = 5
    )
}
