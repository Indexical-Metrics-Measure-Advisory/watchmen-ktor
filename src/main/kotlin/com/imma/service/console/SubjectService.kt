package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Subject
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import kotlin.contracts.ExperimentalContracts

class SubjectService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveSubject(subject: Subject) {
        val fake = determineFakeOrNullId({ subject.subjectId },
            true,
            { subject.subjectId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(subject, Subject::class.java, CollectionNames.SUBJECT)
        } else {
            updateTuple(subject, Subject::class.java, CollectionNames.SUBJECT)
        }

        services.report {
            saveReports(subject.reports.onEach {
                it.connectId = subject.connectId
                it.subjectId = subject.subjectId
                it.userId = subject.userId
            })
        }
    }

    fun findSubjectById(subjectId: String): Subject? {
        return persist().findById(subjectId, Subject::class.java, CollectionNames.SUBJECT)
    }

    fun renameSubject(subjectId: String, name: String?) {
        persist().updateOne(
            where { factor("subjectId") eq { value(subjectId) } },
            update { set("name") to name },
            Subject::class.java, CollectionNames.SUBJECT
        )
    }

    fun deleteSubject(subjectId: String) {
        // delete reports
        services.report {
            deleteReportsBySubject(subjectId)
        }
        // delete subject
        persist().delete(
            where {
                factor("subjectId") eq { value(subjectId) }
            },
            Subject::class.java, CollectionNames.SUBJECT
        )
    }

    @ExperimentalContracts
    fun saveSubjects(subjects: List<Subject>) {
        subjects.forEach { saveSubject(it) }
    }

    fun listSubjectsByConnectedSpaces(connectedSpaceIds: List<String>): List<Subject> {
        val subjects = persist().list(
            where {
                factor("connectId") existsIn { value(connectedSpaceIds) }
            },
            Subject::class.java, CollectionNames.SUBJECT
        )

        val reports = services.report { listReportsByConnectedSpaces(connectedSpaceIds) }

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
        persist().delete(
            where {
                factor("connectId") eq { value(connectId) }
            },
            Subject::class.java, CollectionNames.SUBJECT
        )
    }

    fun isSubjectBelongsTo(subjectId: String, userId: String): Boolean {
        return persist().exists(
            where {
                factor("subjectId") eq { value(subjectId) }
                factor("userId") eq { value(userId) }
            },
            Subject::class.java, CollectionNames.SUBJECT
        )
    }
}