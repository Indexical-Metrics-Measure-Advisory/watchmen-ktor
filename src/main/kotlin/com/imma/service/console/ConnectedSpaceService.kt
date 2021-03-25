package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.ConnectedSpace
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import kotlin.contracts.ExperimentalContracts

class ConnectedSpaceService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveConnectedSpace(connectedSpace: ConnectedSpace) {
        val fake = determineFakeOrNullId({ connectedSpace.connectId },
            true,
            { connectedSpace.connectId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(connectedSpace, ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE)
        } else {
            updateTuple(connectedSpace, ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE)
        }

        services.subject {
            saveSubjects(connectedSpace.subjects.onEach {
                it.connectId = connectedSpace.connectId
                it.userId = connectedSpace.userId
            })
        }
    }

    fun renameConnectedSpace(connectId: String, name: String?) {
        persist().updateOne(
            where {
                column("connectId") eq connectId
            },
            update {
                set("name") to name
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE
        )
    }

    fun deleteConnectedSpace(connectId: String) {
        // delete graphics
        services.connectedSpaceGraphics { deleteConnectedSpaceGraphics(connectId) }
        // delete reports
        services.report { deleteReportsByConnectedSpace(connectId) }
        // delete subjects
        services.subject { deleteSubjectsByConnectedSpace(connectId) }
        // delete connected space
        persist().delete(
            where {
                column("connectId") eq connectId
            },
            ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE
        )
    }

    fun listConnectedSpaceByUser(userId: String): List<ConnectedSpace> {
        val connectedSpaces = persist().list(
            where {
                column("userId") eq userId
            },
            ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE
        )

        val connectedSpaceIds = connectedSpaces.map { it.connectId!! }
        val subjects = services.subject { listSubjectsByConnectedSpaces(connectedSpaceIds) }

        // assemble subjects to connected spaces
        val connectedSpaceMap = connectedSpaces.map { it.connectId to it }.toMap()
        subjects.forEach { subject ->
            val subjectId = subject.connectId
            val connectedSpace = connectedSpaceMap[subjectId]!!
            connectedSpace.subjects.add(subject)
        }

        return connectedSpaces
    }

    fun isConnectedSpaceBelongsTo(connectId: String, userId: String): Boolean {
        return persist().exists(
            where {
                column("connectId") eq connectId
                column("userId") eq userId
            },
            ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE
        )
    }
}

