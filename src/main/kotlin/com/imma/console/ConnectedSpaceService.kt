package com.imma.console

import com.imma.model.*
import com.imma.service.Service
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import kotlin.contracts.ExperimentalContracts

class ConnectedSpaceService(application: Application) : Service(application) {
    private fun createConnectedSpace(connectedSpace: ConnectedSpace) {
        forceAssignDateTimePair(connectedSpace)
        this.writeIntoMongo { it.insert(connectedSpace) }
    }

    private fun updateConnectedSpace(connectedSpace: ConnectedSpace) {
        assignDateTimePair(connectedSpace)
        writeIntoMongo { it.save(connectedSpace) }
    }

    @ExperimentalContracts
    fun saveConnectedSpace(connectedSpace: ConnectedSpace) {
        val fake = determineFakeOrNullId({ connectedSpace.connectId },
            true,
            { connectedSpace.connectId = nextSnowflakeId().toString() })

        if (fake) {
            createConnectedSpace(connectedSpace)
        } else {
            updateConnectedSpace(connectedSpace)
        }

        SubjectService(application).saveSubjects(connectedSpace.subjects.onEach {
            it.connectId = connectedSpace.connectId
        })
    }

    fun isConnectedSpaceBelongsTo(connectId: String, userId: String): Boolean {
        return getFromMongo {
            it.exists(
                Query.query(Criteria.where("connectId").`is`(connectId).and("userId").`is`(userId)),
                ConnectedSpace::class.java,
                CollectionNames.CONNECTED_SPACE
            )
        }
    }

    fun renameConnectedSpace(connectId: String, name: String? = "") {
        writeIntoMongo {
            it.updateFirst(
                Query.query(Criteria.where("connectId").`is`(connectId)),
                Update().set("name", name)
                    .set("lastModifyTime", getCurrentDateTimeAsString())
                    .set("lastModified", getCurrentDateTime()),
                ConnectedSpace::class.java,
                CollectionNames.CONNECTED_SPACE
            )
        }
    }

    fun deleteConnectedSpace(connectId: String) {
        writeIntoMongo {
            // delete reports
            ReportService(application).deleteReportsByConnectedSpace(connectId)
            // delete subjects
            SubjectService(application).deleteSubjectsByConnectedSpace(connectId)
            // delete connected space
            it.remove(
                Query.query(Criteria.where("connectId").`is`(connectId)),
                ConnectedSpace::class.java,
                CollectionNames.CONNECTED_SPACE
            )
        }
    }

    fun listConnectedSpaceByUser(userId: String): List<ConnectedSpace> {
        val query: Query = Query.query(Criteria.where("userId").`is`(userId))
        val connectedSpaces = findListFromMongo(ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE, query)

        val connectedSpaceIds = connectedSpaces.map { it.connectId!! }
        val subjects = SubjectService(application).listSubjectsByConnectedSpaces(connectedSpaceIds)

        // assemble subjects to connected spaces
        val connectedSpaceMap = connectedSpaces.map { it.connectId to it }.toMap()
        subjects.forEach { subject ->
            val subjectId = subject.connectId
            val connectedSpace = connectedSpaceMap[subjectId]!!
            connectedSpace.subjects.add(subject)
        }

        return connectedSpaces
    }
}

