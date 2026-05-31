package com.hotguy.workshopmanagement.domain.usecase.report

import com.hotguy.workshopmanagement.domain.model.SummaryReport
import com.hotguy.workshopmanagement.domain.repository.ReportRepository
import javax.inject.Inject

/**
 * Use Case: obtener el resumen estadístico general del taller.
 *
 * Solo accesible por ADMIN y MECHANIC. La verificación de rol la gestiona
 * el servidor; en el cliente, la pantalla de Dashboard no aparece en el
 * NavGraph cuando el rol es CLIENT.
 */
class GetSummaryReportUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(): Result<SummaryReport> = reportRepository.getSummaryReport()
}
