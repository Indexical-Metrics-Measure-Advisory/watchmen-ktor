package com.imma.console

import com.imma.model.*
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import kotlin.contracts.ExperimentalContracts

class SubjectService(application: Application) : Service(application) {
    private fun createSubject(subject: Subject) {
        forceAssignDateTimePair(subject)
        this.writeIntoMongo { it.insert(subject) }
    }

    private fun updateSubject(subject: Subject) {
        assignDateTimePair(subject)
        writeIntoMongo { it.save(subject) }
    }

    @ExperimentalContracts
    fun saveSubjects(subjects: List<Subject>) {
        val reports = subjects.flatMap { subject ->
            val fake = determineFakeOrNullId({ subject.subjectId },
                true,
                { subject.subjectId = nextSnowflakeId().toString() })

            if (fake) {
                createSubject(subject)
            } else {
                updateSubject(subject)
            }

            subject.reports.onEach {
                it.connectId = subject.connectId
                it.subjectId = subject.subjectId
            }
        }
        ReportService(application).saveReports(reports)
    }

    fun listSubjectsByConnectedSpaces(connectedSpaceIds: List<String>): List<Subject> {
        val query: Query = Query.query(Criteria.where("connectId").`in`(connectedSpaceIds))
        val subjects = findListFromMongo(Subject::class.java, CollectionNames.SUBJECT, query)

        val reports = ReportService(application).listReportsByConnectedSpaces(connectedSpaceIds)

        // assemble reports to subjects
        val subjectMap = subjects.map { it.subjectId to it }.toMap()
        reports.forEach { report ->
            val subjectId = report.subjectId
            val subject = subjectMap[subjectId]!!
            subject.reports.add(report)
        }

        return subjects
    }

    fun deleteSubjectsByConnectedSpace(connectId: String) {
        writeIntoMongo {
            it.remove(
                Query.query(Criteria.where("connectId").`is`(connectId)),
                Subject::class.java,
                CollectionNames.SUBJECT
            )
        }
    }
}