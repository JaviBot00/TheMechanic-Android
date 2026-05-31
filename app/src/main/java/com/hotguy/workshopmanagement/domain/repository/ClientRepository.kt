package com.hotguy.workshopmanagement.domain.repository

import androidx.paging.PagingData
import com.hotguy.workshopmanagement.domain.model.Client
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de clientes.
 *
 * Define todas las operaciones CRUD sobre clientes que existen en el sistema.
 * Los parámetros de los métodos de escritura (crear, actualizar) se pasan
 * como tipos primitivos o data classes del dominio, nunca como DTOs de Retrofit.
 *
 * Nota sobre paginación: los métodos de listado devuelven [Flow<PagingData<Client>>]
 * en lugar de [Result<List<Client>>]. Paging 3 de Jetpack gestiona automáticamente
 * la carga incremental de páginas, los estados de carga/error y el caché en memoria.
 * El ViewModel expone este Flow directamente a la UI mediante [collectAsLazyPagingItems()].
 */
interface ClientRepository {

    /**
     * Stream paginado de todos los clientes activos, ordenados por apellido.
     * La paginación se gestiona automáticamente con Paging 3.
     */
    fun getClients(): Flow<PagingData<Client>>

    /**
     * Stream paginado de clientes cuyo primer apellido contiene [query].
     * Búsqueda insensible a mayúsculas/minúsculas.
     *
     * @param query Texto a buscar en el primer apellido.
     */
    fun searchClientsBySurname(query: String): Flow<PagingData<Client>>

    /**
     * Obtiene un cliente por su identificador técnico.
     *
     * @param id Identificador del cliente.
     * @return [Result] con el [Client] o con la excepción (404, red...).
     */
    suspend fun getClientById(id: Long): Result<Client>

    /**
     * Obtiene un cliente por su NIF.
     *
     * @param nif NIF del cliente en formato "12345678A".
     * @return [Result] con el [Client] o con la excepción si no existe.
     */
    suspend fun getClientByNif(nif: String): Result<Client>

    /**
     * Crea un nuevo cliente en el sistema.
     *
     * @param clientCode Código de cliente asignado por el taller.
     * @param name       Nombre de pila.
     * @param surname1   Primer apellido.
     * @param surname2   Segundo apellido (opcional).
     * @param nif        NIF en formato "12345678A".
     * @param email      Correo electrónico.
     * @param telephone  Teléfono de contacto (opcional).
     * @return [Result] con el [Client] creado (incluye el id generado por el servidor).
     */
    suspend fun createClient(
        clientCode: Int,
        name:       String,
        surname1:   String,
        surname2:   String?,
        nif:        String,
        email:      String,
        telephone:  String?
    ): Result<Client>

    /**
     * Actualiza los datos de un cliente existente.
     *
     * @param id         Identificador del cliente a actualizar.
     * @param clientCode Nuevo código de cliente.
     * @param name       Nuevo nombre de pila.
     * @param surname1   Nuevo primer apellido.
     * @param surname2   Nuevo segundo apellido (opcional).
     * @param nif        Nuevo NIF.
     * @param email      Nuevo correo electrónico.
     * @param telephone  Nuevo teléfono (opcional).
     * @return [Result] con el [Client] actualizado.
     */
    suspend fun updateClient(
        id:         Long,
        clientCode: Int,
        name:       String,
        surname1:   String,
        surname2:   String?,
        nif:        String,
        email:      String,
        telephone:  String?
    ): Result<Client>

    /**
     * Elimina lógicamente un cliente (soft delete).
     * El registro no se borra de la base de datos; queda marcado como inactivo.
     * Solo ADMIN puede llamar a esta operación.
     *
     * @param id Identificador del cliente a eliminar.
     * @return [Result.success] si la operación fue correcta.
     */
    suspend fun deleteClient(id: Long): Result<Unit>
}
