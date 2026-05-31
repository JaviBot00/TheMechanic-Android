package com.hotguy.workshopmanagement.domain.usecase

import com.hotguy.workshopmanagement.domain.model.TaskStatus
import com.hotguy.workshopmanagement.domain.model.WorkshopTask
import com.hotguy.workshopmanagement.domain.repository.TaskRepository
import com.hotguy.workshopmanagement.domain.usecase.task.AddHoursToTaskUseCase
import com.hotguy.workshopmanagement.domain.usecase.task.CreateTaskUseCase
import com.hotguy.workshopmanagement.domain.usecase.task.MarkTaskAsPaidUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Tests unitarios para los use cases de tareas.
 *
 * Se prueban las reglas de dominio que viven en los use cases (validaciones
 * de horas, estado de la tarea) y la delegación correcta al repositorio.
 */
class TaskUseCaseTest {

    private val taskRepository: TaskRepository = mockk()

    private lateinit var addHoursUseCase:  AddHoursToTaskUseCase
    private lateinit var markAsPaidUseCase: MarkTaskAsPaidUseCase
    private lateinit var createTaskUseCase: CreateTaskUseCase

    /** Tarea de prueba reutilizada en varios tests. */
    private val sampleTask = WorkshopTask(
        id           = 1L,
        diagnostic   = "Revisión de frenos",
        solution     = null,
        previewHours = 4f,
        realHours    = 2f,
        progress     = 50f,
        status       = TaskStatus.IN_PROGRESS,
        estimatedCost = 100f,
        totalCost    = 0f,
        finished     = false,
        paid         = false,
        initDate     = LocalDate.now(),
        notes        = null,
        clientId     = 1L,
        clientName   = "Juan García",
        vehicleId    = 1L,
        vehicleReg   = "1234ABC",
        mechanicId   = 1L,
        mechanicName = "Pedro Mecánico",
        createdAt    = Instant.now(),
        updatedAt    = Instant.now()
    )

    @Before
    fun setUp() {
        addHoursUseCase  = AddHoursToTaskUseCase(taskRepository)
        markAsPaidUseCase = MarkTaskAsPaidUseCase(taskRepository)
        createTaskUseCase = CreateTaskUseCase(taskRepository)
    }

    // ── AddHoursToTaskUseCase ─────────────────────────────────────────────────

    @Test
    fun `given positive hours, when addHours, then delegates to repository`() = runTest {
        // Arrange
        coEvery { taskRepository.addHours(1L, 1.5f) } returns Result.success(sampleTask)

        // Act
        val result = addHoursUseCase(id = 1L, hours = 1.5f)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { taskRepository.addHours(1L, 1.5f) }
    }

    @Test
    fun `given zero hours, when addHours, then returns failure without calling repository`() = runTest {
        val result = addHoursUseCase(id = 1L, hours = 0f)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("positivo") == true)
        coVerify(exactly = 0) { taskRepository.addHours(any(), any()) }
    }

    @Test
    fun `given negative hours, when addHours, then returns failure without calling repository`() = runTest {
        val result = addHoursUseCase(id = 1L, hours = -2f)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { taskRepository.addHours(any(), any()) }
    }

    // ── MarkTaskAsPaidUseCase ─────────────────────────────────────────────────

    @Test
    fun `given valid task id, when markAsPaid, then delegates to repository`() = runTest {
        // Arrange
        val paidTask = sampleTask.copy(finished = true, paid = true, status = TaskStatus.PAID)
        coEvery { taskRepository.markAsPaid(1L) } returns Result.success(paidTask)

        // Act
        val result = markAsPaidUseCase(id = 1L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(TaskStatus.PAID, result.getOrNull()?.status)
        coVerify(exactly = 1) { taskRepository.markAsPaid(1L) }
    }

    @Test
    fun `given repository returns conflict error, when markAsPaid, then propagates failure`() = runTest {
        // El servidor devuelve 409 si la tarea no está finalizada
        coEvery { taskRepository.markAsPaid(1L) } returns
            Result.failure(Exception("No se puede cobrar una tarea no finalizada"))

        val result = markAsPaidUseCase(id = 1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("finalizada") == true)
    }

    // ── CreateTaskUseCase ─────────────────────────────────────────────────────

    @Test
    fun `given invalid vehicleId, when createTask, then returns failure without calling repository`() = runTest {
        val result = createTaskUseCase(
            vehicleId    = 0L,
            mechanicId   = 1L,
            diagnostic   = "Revisión",
            previewHours = 2f,
            initDate     = LocalDate.now(),
            notes        = null
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("vehículo") == true)
        coVerify(exactly = 0) { taskRepository.createTask(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `given blank diagnostic, when createTask, then returns failure`() = runTest {
        val result = createTaskUseCase(
            vehicleId    = 1L,
            mechanicId   = 1L,
            diagnostic   = "   ",
            previewHours = 2f,
            initDate     = LocalDate.now(),
            notes        = null
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("diagnóstico") == true)
    }

    @Test
    fun `given negative previewHours, when createTask, then returns failure`() = runTest {
        val result = createTaskUseCase(
            vehicleId    = 1L,
            mechanicId   = 1L,
            diagnostic   = "Diagnóstico válido",
            previewHours = -1f,
            initDate     = LocalDate.now(),
            notes        = null
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("positivas") == true)
    }

    @Test
    fun `given valid params, when createTask, then delegates to repository with trimmed diagnostic`() = runTest {
        // Arrange
        coEvery {
            taskRepository.createTask(1L, 2L, "Revisión de frenos", 3f, any(), null)
        } returns Result.success(sampleTask)

        // Act — el diagnóstico tiene espacios extra
        val result = createTaskUseCase(
            vehicleId    = 1L,
            mechanicId   = 2L,
            diagnostic   = "  Revisión de frenos  ",
            previewHours = 3f,
            initDate     = LocalDate.now(),
            notes        = null
        )

        // Assert — el repositorio recibe el diagnóstico limpio
        assertTrue(result.isSuccess)
        coVerify { taskRepository.createTask(1L, 2L, "Revisión de frenos", 3f, any(), null) }
    }
}
