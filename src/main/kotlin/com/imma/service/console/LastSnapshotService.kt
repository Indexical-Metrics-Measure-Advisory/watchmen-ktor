package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.LastSnapshot
import com.imma.service.Service
import com.imma.service.Services

class LastSnapshotService(services: Services) : Service(services) {
	fun findLastSnapshotById(userId: String): LastSnapshot? {
		return persist().findById(userId, LastSnapshot::class.java, CollectionNames.LAST_SNAPSHOT)
	}

	fun saveLastSnapshot(lastSnapshot: LastSnapshot) {
		persist().upsertOne(lastSnapshot, LastSnapshot::class.java, CollectionNames.LAST_SNAPSHOT)
	}
}