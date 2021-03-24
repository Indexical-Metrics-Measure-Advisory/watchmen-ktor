package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Report
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import kotlin.contracts.ExperimentalContracts

class ReportService(application: Application) : TupleService(application) {
    @ExperimentalContracts
    fun saveReport(report: Report) {
        val fake = determineFakeOrNullId({ report.reportId },
            true,
            { report.reportId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(report)
        } else {
            updateTuple(report)
        }
    }

    fun findReportById(reportId: String): Report? {
        return persistKit.findById(reportId, Report::class.java, CollectionNames.REPORT)
    }

    fun renameReport(reportId: String, name: String? = "") {
        writeIntoMongo {
            it.updateFirst(
                Query.query(Criteria.where("reportId").`is`(reportId)),
                Update().apply {
                    set("name", name)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                Report::class.java,
                CollectionNames.REPORT
            )
        }
    }

    fun deleteReport(reportId: String) {
        writeIntoMongo {
            it.remove(
                Query.query(Criteria.where("reportId").`is`(reportId)),
                Report::class.java,
                CollectionNames.REPORT
            )
        }
    }

    @ExperimentalContracts
    fun saveReports(reports: List<Report>) {
        reports.forEach { saveReport(it) }
    }

    fun listReportsByConnectedSpaces(connectedSpaceIds: List<String>): List<Report> {
        val query: Query = Query.query(Criteria.where("connectId").`in`(connectedSpaceIds))
        return findListFromMongo(Report::class.java, CollectionNames.REPORT, query)
    }

    fun deleteReportsByConnectedSpace(connectId: String) {
        writeIntoMongo {
            it.remove(
                Query.query(Criteria.where("connectId").`is`(connectId)),
                Report::class.java,
                CollectionNames.REPORT
            )
        }
    }

    fun deleteReportsBySubject(subjectId: String) {
        writeIntoMongo {
            it.remove(
                Query.query(Criteria.where("subjectId").`is`(subjectId)),
                Report::class.java,
                CollectionNames.REPORT
            )
        }
    }

    fun isReportBelongsTo(reportId: String, userId: String): Boolean {
        return getFromMongo {
            it.exists(
                Query.query(Criteria.where("reportId").`is`(reportId).and("userId").`is`(userId)),
                Report::class.java,
                CollectionNames.REPORT
            )
        }
    }

    fun findReportsByName(name: String?, pageable: Pageable): DataPage<Report> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(Report::class.java, CollectionNames.REPORT, query, pageable)
    }
}