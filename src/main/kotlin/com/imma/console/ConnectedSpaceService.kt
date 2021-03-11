package com.imma.console

import com.imma.model.*
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
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
}

