package com.imma.console

import com.imma.model.*
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
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

    fun listReportBySubjects(subjectIds: List<String>): List<Report> {
        val query: Query = Query.query(Criteria.where("subjectId").`in`(subjectIds))
        return findListFromMongo(Report::class.java, CollectionNames.REPORT, query)
    }
}