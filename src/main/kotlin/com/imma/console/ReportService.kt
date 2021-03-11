package com.imma.console

import com.imma.model.Report
import com.imma.model.assignDateTimePair
import com.imma.model.determineFakeOrNullId
import com.imma.model.forceAssignDateTimePair
import com.imma.service.Service
import io.ktor.application.*
import kotlin.contracts.ExperimentalContracts

class ReportService(application: Application) : Service(application) {
    private fun createReport(report: Report) {
        forceAssignDateTimePair(report)
        this.writeIntoMongo { it.insert(report) }
    }

    private fun updateReport(report: Report) {
        assignDateTimePair(report)
        writeIntoMongo { it.save(report) }
    }

    @ExperimentalContracts
    fun saveReports(reports: List<Report>) {
        reports.forEach { report ->
            val fake = determineFakeOrNullId({ report.reportId },
                true,
                { report.reportId = nextSnowflakeId().toString() })

            if (fake) {
                createReport(report)
            } else {
                updateReport(report)
            }
        }
    }
}