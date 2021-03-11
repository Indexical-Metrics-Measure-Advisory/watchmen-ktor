package com.imma.console

import com.imma.model.ConnectedSpace
import com.imma.model.assignDateTimePair
import com.imma.model.determineFakeOrNullId
import com.imma.model.forceAssignDateTimePair
import com.imma.service.Service
import io.ktor.application.*
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
}

