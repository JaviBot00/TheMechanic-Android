package com.hotguy.workshopmanagement.domain.usecase

import com.hotguy.workshopmanagement.domain.model.Client
import com.hotguy.workshopmanagement.domain.repository.ClientRepository
import com.hotguy.workshopmanagement.domain.usecase.client.CreateClientUseCase
import com.hotguy.workshopmanagement.domain.usecase.client.SearchClientsBySurnameUseCase
import com.hotguy.workshopmanagement.domain.usecase.client.UpdateClientUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Tests unitarios para los use cases de clientes.
 *
 * Se prueban las validaciones de dominio (campos obligatorios) y la
 * normalización del NIF a mayúsculas antes de enviarlo al repositorio.
 */
class ClientUseCaseTest {

    private val clientRepository: ClientRepository = mockk()
    private lateinit var createClientUseCase:         CreateClientUseCase
    private lateinit var updateClientUseCase:         UpdateClientUseCase
    private lateinit var searchClientsBySurnameUseCase: SearchClientsBySurnameUseCase

    private val sampleClient = Client(
        id           = 1L,
        clientCode   = 100,
        name         = "Juan",
        surname1     = "García",
        surname2     = null,
        nif          = "12345678A",
        email        = "juan@test.com",
        telephone    = null,
        vehicleCount = 0,
        createdAt    = Instant.now(),
        updatedAt    = Instant.now()
    )

    @Before
    fun setUp() {
        createClientUseCase          = CreateClientUseCase(clientRepository)
        updateClientUseCase          = UpdateClientUseCase(clientRepository)
        searchClientsBySurnameUseCase = SearchClientsBySurnameUseCase(clientRepository)
    }

    // ── Validación de campos obligatorios ─────────────────────────────────────

    @Test
    fun `given blank name, when createClient, then returns failure`() = runTest {
        val result = createClientUseCase(
            clientCode = 100, name = "", surname1 = "García",
            surname2 = null, nif = "12345678A", email = "juan@test.com", telephone = null
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("nombre") == true)
        coVerify(exactly = 0) { clientRepository.createClient(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `given blank surname1, when createClient, then returns failure`() = runTest {
        val result = createClientUseCase(
            clientCode = 100, name = "Juan", surname1 = "   ",
            surname2 = null, nif = "12345678A", email = "juan@test.com", telephone = null
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("apellido") == true)
    }

    @Test
    fun `given blank nif, when createClient, then returns failure`() = runTest {
        val result = createClientUseCase(
            clientCode = 100, name = "Juan", surname1 = "García",
            surname2 = null, nif = "", email = "juan@test.com", telephone = null
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("NIF") == true)
    }

    @Test
    fun `given blank email, when createClient, then returns failure`() = runTest {
        val result = createClientUseCase(
            clientCode = 100, name = "Juan", surname1 = "García",
            surname2 = null, nif = "12345678A", email = "   ", telephone = null
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("email") == true)
    }

    // ── Normalización de NIF ──────────────────────────────────────────────────

    @Test
    fun `given lowercase nif, when createClient, then normalizes to uppercase before calling repository`() = runTest {
        coEvery {
            clientRepository.createClient(100, "Juan", "García", null, "12345678A", "juan@test.com", null)
        } returns Result.success(sampleClient)

        // Act — NIF en minúsculas
        val result = createClientUseCase(
            clientCode = 100, name = "Juan", surname1 = "García",
            surname2 = null, nif = "12345678a", email = "juan@test.com", telephone = null
        )

        // Assert — repositorio recibe NIF en mayúsculas
        assertTrue(result.isSuccess)
        coVerify { clientRepository.createClient(100, "Juan", "García", null, "12345678A", "juan@test.com", null) }
    }

    @Test
    fun `given nif with spaces, when createClient, then trims before calling repository`() = runTest {
        coEvery {
            clientRepository.createClient(100, "Juan", "García", null, "12345678A", "juan@test.com", null)
        } returns Result.success(sampleClient)

        val result = createClientUseCase(
            clientCode = 100, name = "Juan", surname1 = "García",
            surname2 = null, nif = "  12345678a  ", email = "juan@test.com", telephone = null
        )

        assertTrue(result.isSuccess)
        coVerify { clientRepository.createClient(100, "Juan", "García", null, "12345678A", "juan@test.com", null) }
    }

    // ── Delegación correcta al repositorio ────────────────────────────────────

    @Test
    fun `given valid data, when createClient, then delegates to repository and returns success`() = runTest {
        coEvery {
            clientRepository.createClient(any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(sampleClient)

        val result = createClientUseCase(
            clientCode = 100, name = "Juan", surname1 = "García",
            surname2 = "López", nif = "12345678A", email = "juan@test.com", telephone = "600000000"
        )

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            clientRepository.createClient(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `given repository failure, when createClient, then propagates failure`() = runTest {
        coEvery {
            clientRepository.createClient(any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("NIF ya registrado"))

        val result = createClientUseCase(
            clientCode = 100, name = "Juan", surname1 = "García",
            surname2 = null, nif = "12345678A", email = "juan@test.com", telephone = null
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("NIF ya registrado") == true)
    }

    // ── SearchClientsBySurnameUseCase ─────────────────────────────────────────

    @Test
    fun `given blank query, when searchBySurname, then calls getClients instead of search`() {
        // El mock está relaxado para Flow; solo verificamos que no lanza excepción
        every { clientRepository.getClients() } returns mockk(relaxed = true)
        every { clientRepository.searchClientsBySurname(any()) } returns mockk(relaxed = true)

        // Act
        searchClientsBySurnameUseCase("   ")

        // Assert — usa getClients (lista completa) en lugar de search
        io.mockk.verify(exactly = 1) { clientRepository.getClients() }
        io.mockk.verify(exactly = 0) { clientRepository.searchClientsBySurname(any()) }
    }

    @Test
    fun `given non-blank query, when searchBySurname, then calls searchClientsBySurname with trimmed query`() {
        every { clientRepository.searchClientsBySurname("García") } returns mockk(relaxed = true)

        searchClientsBySurnameUseCase("  García  ")

        io.mockk.verify(exactly = 1) { clientRepository.searchClientsBySurname("García") }
    }
}
