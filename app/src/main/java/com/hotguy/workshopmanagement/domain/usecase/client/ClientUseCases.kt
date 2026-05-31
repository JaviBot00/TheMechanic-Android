package com.hotguy.workshopmanagement.domain.usecase.client

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.Client
import com.hotguy.workshopmanagement.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case: obtener la lista paginada de todos los clientes activos.
 *
 * Devuelve un [Flow] en lugar de un valor puntual porque Paging 3 necesita
 * un stream activo para cargar páginas a medida que el usuario hace scroll.
 * El ViewModel colecta este Flow y lo expone a la UI con
 * `collectAsLazyPagingItems()`.
 */
class GetClientsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    operator fun invoke(): Flow<PagingData<Client>> = clientRepository.getClients()
}

/**
 * Use Case: buscar clientes por primer apellido.
 *
 * @param query Texto a buscar. Si está vacío, delega en [GetClientsUseCase]
 *              para mostrar todos los clientes en lugar de una lista vacía.
 */
class SearchClientsBySurnameUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Client>> {
        // Si la búsqueda está vacía, devolver todos los clientes
        return if (query.isBlank()) {
            clientRepository.getClients()
        } else {
            clientRepository.searchClientsBySurname(query.trim())
        }
    }
}

/**
 * Use Case: obtener un cliente concreto por su ID.
 *
 * @param id Identificador técnico del cliente.
 */
class GetClientByIdUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(id: Long): Result<Client> = clientRepository.getClientById(id)
}

/**
 * Use Case: crear un nuevo cliente en el sistema.
 *
 * Valida las reglas de negocio que no puede comprobar la UI:
 * - El NIF debe tener el formato correcto (8 dígitos + letra mayúscula).
 * - El email no puede estar vacío.
 * La validación del formato exacto la hace el backend, pero rechazamos
 * casos obvios aquí para dar feedback rápido al usuario sin llamada de red.
 */
class CreateClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(
        clientCode: Int,
        name:       String,
        surname1:   String,
        surname2:   String?,
        nif:        String,
        email:      String,
        telephone:  String?
    ): Result<Client> {
        if (name.isBlank())     return Result.failure(IllegalArgumentException("El nombre es obligatorio"))
        if (surname1.isBlank()) return Result.failure(IllegalArgumentException("El primer apellido es obligatorio"))
        if (nif.isBlank())      return Result.failure(IllegalArgumentException("El NIF es obligatorio"))
        if (email.isBlank())    return Result.failure(IllegalArgumentException("El email es obligatorio"))

        return clientRepository.createClient(
            clientCode, name.trim(), surname1.trim(),
            surname2?.trim(), nif.trim().uppercase(), email.trim(), telephone?.trim()
        )
    }
}

/**
 * Use Case: actualizar los datos de un cliente existente.
 *
 * Aplica las mismas validaciones que [CreateClientUseCase].
 */
class UpdateClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(
        id:         Long,
        clientCode: Int,
        name:       String,
        surname1:   String,
        surname2:   String?,
        nif:        String,
        email:      String,
        telephone:  String?
    ): Result<Client> {
        if (name.isBlank())     return Result.failure(IllegalArgumentException("El nombre es obligatorio"))
        if (surname1.isBlank()) return Result.failure(IllegalArgumentException("El primer apellido es obligatorio"))
        if (nif.isBlank())      return Result.failure(IllegalArgumentException("El NIF es obligatorio"))
        if (email.isBlank())    return Result.failure(IllegalArgumentException("El email es obligatorio"))

        return clientRepository.updateClient(
            id, clientCode, name.trim(), surname1.trim(),
            surname2?.trim(), nif.trim().uppercase(), email.trim(), telephone?.trim()
        )
    }
}

/**
 * Use Case: eliminar lógicamente un cliente.
 * Solo disponible para ADMIN. La verificación de rol la hace el servidor;
 * en el cliente se controla qué botones son visibles según el rol.
 *
 * @param id Identificador del cliente a eliminar.
 */
class DeleteClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = clientRepository.deleteClient(id)
}
