package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Report
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import kotlin.contracts.ExperimentalContracts

class ReportService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveReport(report: Report) {
        val fake = determineFakeOrNullId({ report.reportId },
            true,
            { report.reportId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(report, Report::class.java, CollectionNames.REPORT)
        } else {
            updateTuple(report, Report::class.java, CollectionNames.REPORT)
        }
    }

    fun findReportById(reportId: String): Report? {
        return persist().findById(reportId, Report::class.java, CollectionNames.REPORT)
    }

    fun renameReport(reportId: String, name: String?) {
        persist().updateOne(
            where {
                column("reportId") eq reportId
            },
            update {
                set("name") to name
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            Report::class.java, CollectionNames.REPORT
        )
    }

    fun deleteReport(reportId: String) {
        persist().delete(
            where {
                column("reportId") eq reportId
            },
            Report::class.java, CollectionNames.REPORT
        )
    }

    @ExperimentalContracts
    fun saveReports(reports: List<Report>) {
        reports.forEach { saveReport(it) }
    }

    fun listReportsByConnectedSpaces(connectedSpaceIds: List<String>): List<Report> {
        return persist().list(
            where {
                column("connectId") `in` connectedSpaceIds
            },
            Report::class.java, CollectionNames.REPORT
        )
    }

    fun deleteReportsByConnectedSpace(connectId: String) {
        persist().delete(
            where {
                column("connectId") eq connectId
            },
            Report::class.java, CollectionNames.REPORT
        )
    }

    fun deleteReportsBySubject(subjectId: String) {
        persist().delete(
            where {
                column("subjectId") eq subjectId
            },
            Report::class.java, CollectionNames.REPORT
        )
    }

    fun isReportBelongsTo(reportId: String, userId: String): Boolean {
        return persist().exists(
            where {
                column("reportId") eq reportId
                column("userId") eq userId
            },
            Report::class.java, CollectionNames.REPORT
        )
    }

    fun findReportsByName(name: String?, pageable: Pageable): DataPage<Report> {
        return if (name.isNullOrEmpty()) {
            persist().page(
                pageable, Report::class.java, CollectionNames.REPORT
            )
        } else {
            persist().page(
                where {
                    column("name") regex name
                },
                pageable,
                Report::class.java, CollectionNames.REPORT
            )
        }
    }
}