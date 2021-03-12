package com.imma.console

import com.imma.model.CollectionNames
import com.imma.model.ConnectedSpace
import com.imma.model.ConnectedSpaceGraphics
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class ConnectedSpaceGraphicsService(application: Application) : Service(application) {
    fun saveConnectedSpaceGraphics(graphics: ConnectedSpaceGraphics) {
        writeIntoMongo {
            it.upsert(
                Query.query(Criteria.where("connectId").`is`(graphics.connectId)),
                Update().apply {
                    set("connectId", graphics.connectId)
                    set("userId", graphics.userId)
                    set("topics", graphics.topics)
                    set("subjects", graphics.subjects)
                    set("reports", graphics.reports)
                },
                ConnectedSpaceGraphics::class.java,
                CollectionNames.CONNECTED_SPACE_GRAPHICS
            )
        }
    }

    fun listConnectedSpaceGraphicsByUser(userId: String): List<ConnectedSpaceGraphics> {
        val query: Query = Query.query(Criteria.where("userId").`is`(userId))
        return findListFromMongo(ConnectedSpaceGraphics::class.java, CollectionNames.CONNECTED_SPACE_GRAPHICS, query)
    }

    fun deleteConnectedSpaceGraphics(connectId: String) {
        writeIntoMongo {
            it.remove(
                Query.query(Criteria.where("connectId").`is`(connectId)),
                ConnectedSpaceGraphics::class.java,
                CollectionNames.CONNECTED_SPACE_GRAPHICS
            )
        }
    }
}