package com.imma.console

import com.imma.model.Subject
import com.imma.model.assignDateTimePair
import com.imma.model.determineFakeOrNullId
import com.imma.model.forceAssignDateTimePair
import com.imma.service.Service
import io.ktor.application.*
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

            subject.reports.onEach { it.subjectId = subject.subjectId }
        }
        ReportService(application).saveReports(reports)
    }
}