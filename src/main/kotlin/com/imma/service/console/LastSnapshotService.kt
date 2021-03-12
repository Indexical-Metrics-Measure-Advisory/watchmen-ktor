package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.LastSnapshot
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class LastSnapshotService(application: Application) : Service(application) {
    fun findById(userId: String): LastSnapshot? {
        return findFromMongo {
            it.findById(userId, LastSnapshot::class.java, CollectionNames.LAST_SNAPSHOT)
        }
    }

    fun saveLastSnapshot(lastSnapshot: LastSnapshot) {
        writeIntoMongo {
            it.upsert(
                Query.query(Criteria.where("userId").`is`(lastSnapshot.userId)),
                Update().apply {
                    set("userId", lastSnapshot.userId)
                    set("language", lastSnapshot.language)
                    set("lastDashboardId", lastSnapshot.lastDashboardId)
                    set("favoritePin", lastSnapshot.favoritePin)
                },
                LastSnapshot::class.java,
                CollectionNames.LAST_SNAPSHOT
            )
        }
    }
}