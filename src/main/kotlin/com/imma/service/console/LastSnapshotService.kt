package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.LastSnapshot
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Service
import com.imma.service.Services

class LastSnapshotService(services: Services) : Service(services) {
    fun findLastSnapshotById(userId: String): LastSnapshot? {
        return persist().findById(userId, LastSnapshot::class.java, CollectionNames.LAST_SNAPSHOT)
    }

    fun saveLastSnapshot(lastSnapshot: LastSnapshot) {
        persist().upsert(
            where {
                column("userId") eq lastSnapshot.userId
            },
            update {
                set("userId") to lastSnapshot.userId
                set("language") to lastSnapshot.language
                set("lastDashboardId") to lastSnapshot.lastDashboardId
                set("adminDashboardId") to lastSnapshot.adminDashboardId
                set("favoritePin") to lastSnapshot.favoritePin
            },
            LastSnapshot::class.java, CollectionNames.LAST_SNAPSHOT
        )
    }
}