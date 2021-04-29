package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.ConnectedSpace
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
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
			where { factor("connectId") eq { value(connectId) } },
			update { set("name") to name },
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
		persist().deleteById(connectId, ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE)
	}

	fun listConnectedSpaceByUser(userId: String): List<ConnectedSpace> {
		val connectedSpaces: List<ConnectedSpace> = persist().list(
			where {
				factor("userId") eq { value(userId) }
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
				factor("connectId") eq { value(connectId) }
				factor("userId") eq { value(userId) }
			},
			ConnectedSpace::class.java, CollectionNames.CONNECTED_SPACE
		)
	}
}

