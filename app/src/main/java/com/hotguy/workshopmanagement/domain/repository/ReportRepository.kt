package com.hotguy.workshopmanagement.domain.repository

import com.hotguy.workshopmanagement.domain.model.SummaryReport

/**
 * Contrato del repositorio de reportes.
 *
 * Actualmente expone un único endpoint de resumen. Si el backend creciera
 * con más tipos de reporte (por periodo, por mecánico, etc.), se añadirían
 * nuevos métodos aquí sin afectar al resto de capas.
 */
interface ReportRepository {

    /**
     * Obtiene el resumen estadístico general del taller.
     * Solo accesible por ADMIN y MECHANIC.
     *
     * @return [Result] con el [SummaryReport] o con la excepción si hay error.
     */
    suspend fun getSummaryReport(): Result<SummaryReport>
}
