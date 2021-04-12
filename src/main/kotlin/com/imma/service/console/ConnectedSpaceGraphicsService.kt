package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.ConnectedSpaceGraphics
import com.imma.persist.core.where
import com.imma.service.Service
import com.imma.service.Services

class ConnectedSpaceGraphicsService(services: Services) : Service(services) {
    fun saveConnectedSpaceGraphics(graphics: ConnectedSpaceGraphics) {
        persist().upsertOne(graphics, ConnectedSpaceGraphics::class.java, CollectionNames.CONNECTED_SPACE_GRAPHICS)
    }

    fun listConnectedSpaceGraphicsByUser(userId: String): List<ConnectedSpaceGraphics> {
        return persist().list(
            where {
                factor("userId") eq { value(userId) }
            },
            ConnectedSpaceGraphics::class.java, CollectionNames.CONNECTED_SPACE_GRAPHICS
        )
    }

    fun deleteConnectedSpaceGraphics(connectId: String) {
        persist().deleteById(connectId, ConnectedSpaceGraphics::class.java, CollectionNames.CONNECTED_SPACE_GRAPHICS)
    }
}