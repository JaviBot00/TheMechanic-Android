package com.hotguy.workshopmanagement.domain.usecase

import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.domain.model.VehicleType
import com.hotguy.workshopmanagement.domain.repository.VehicleRepository
import com.hotguy.workshopmanagement.domain.usecase.vehicle.CreateVehicleUseCase
import com.hotguy.workshopmanagement.domain.usecase.vehicle.UpdateVehicleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Tests unitarios para los use cases de vehículos.
 * Se prueban las reglas de dominio: validación de campos obligatorios y
 * normalización de la matrícula a mayúsculas.
 */
class VehicleUseCaseTest {

    private val vehicleRepository: VehicleRepository = mockk()
    private lateinit var createVehicleUseCase: CreateVehicleUseCase
    private lateinit var updateVehicleUseCase: UpdateVehicleUseCase

    private val sampleVehicle = Vehicle(
        id               = 1L,
        registrationCode = "1234ABC",
        model            = "Toyota Corolla",
        type             = VehicleType.CAR,
        clientId         = 1L,
        clientName       = "Juan García",
        taskCount        = 0,
        completionPct    = 0f,
        totalRevenue     = 0f,
        createdAt        = Instant.now(),
        updatedAt        = Instant.now()
    )

    @Before
    fun setUp() {
        createVehicleUseCase = CreateVehicleUseCase(vehicleRepository)
        updateVehicleUseCase = UpdateVehicleUseCase(vehicleRepository)
    }

    // ── Validaciones de matrícula ──────────────────────────────────────────────

    @Test
    fun `given blank registration code, when createVehicle, then returns failure`() = runTest {
        val result = createVehicleUseCase(
            registrationCode = "",
            model            = "Toyota Corolla",
            type             = VehicleType.CAR,
            clientId         = 1L
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("matrícula") == true)
        coVerify(exactly = 0) { vehicleRepository.createVehicle(any(), any(), any(), any()) }
    }

    @Test
    fun `given lowercase registration code, when createVehicle, then normalizes to uppercase`() = runTest {
        // Arrange
        coEvery {
            vehicleRepository.createVehicle("1234ABC", "Toyota Corolla", VehicleType.CAR, 1L)
        } returns Result.success(sampleVehicle)

        // Act — matrícula en minúsculas
        val result = createVehicleUseCase(
            registrationCode = "1234abc",
            model            = "Toyota Corolla",
            type             = VehicleType.CAR,
            clientId         = 1L
        )

        // Assert — el repositorio recibe la matrícula en mayúsculas
        assertTrue(result.isSuccess)
        coVerify { vehicleRepository.createVehicle("1234ABC", "Toyota Corolla", VehicleType.CAR, 1L) }
    }

    @Test
    fun `given blank model, when createVehicle, then returns failure`() = runTest {
        val result = createVehicleUseCase(
            registrationCode = "1234ABC",
            model            = "   ",
            type             = VehicleType.CAR,
            clientId         = 1L
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("modelo") == true)
    }

    @Test
    fun `given invalid clientId, when createVehicle, then returns failure`() = runTest {
        val result = createVehicleUseCase(
            registrationCode = "1234ABC",
            model            = "Toyota Corolla",
            type             = VehicleType.CAR,
            clientId         = -1L
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("propietario") == true)
    }

    // ── VehicleType pricing logic ──────────────────────────────────────────────

    @Test
    fun `MOTORCYCLE has correct hourly rate and zero fixed fee`() {
        assertEquals(20f, VehicleType.MOTORCYCLE.hourlyRate)
        assertEquals(0f,  VehicleType.MOTORCYCLE.fixedFee)
    }

    @Test
    fun `CAR has correct hourly rate and zero fixed fee`() {
        assertEquals(25f, VehicleType.CAR.hourlyRate)
        assertEquals(0f,  VehicleType.CAR.fixedFee)
    }

    @Test
    fun `VAN has correct hourly rate and fixed fee`() {
        assertEquals(30f, VehicleType.VAN.hourlyRate)
        assertEquals(30f, VehicleType.VAN.fixedFee)
    }

    @Test
    fun `TRUCK has correct hourly rate and fixed fee`() {
        assertEquals(40f, VehicleType.TRUCK.hourlyRate)
        assertEquals(50f, VehicleType.TRUCK.fixedFee)
    }

    @Test
    fun `VehicleType fromApi returns correct enum for known values`() {
        assertEquals(VehicleType.CAR,        VehicleType.fromApi("CAR"))
        assertEquals(VehicleType.MOTORCYCLE, VehicleType.fromApi("MOTORCYCLE"))
        assertEquals(VehicleType.VAN,        VehicleType.fromApi("VAN"))
        assertEquals(VehicleType.TRUCK,      VehicleType.fromApi("TRUCK"))
    }

    @Test
    fun `VehicleType fromApi returns CAR as fallback for unknown value`() {
        assertEquals(VehicleType.CAR, VehicleType.fromApi("UNKNOWN_TYPE"))
    }
}
