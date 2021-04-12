package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.core.PipelineGraphics
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Service
import com.imma.service.Services

class PipelineGraphicsService(services: Services) : Service(services) {
    fun findPipelineGraphicsById(userId: String): PipelineGraphics? {
        return persist().findById(userId, PipelineGraphics::class.java, CollectionNames.PIPELINE_GRAPHICS)
    }

    fun savePipelineGraphicsByUser(graphics: PipelineGraphics) {
        persist().upsert(
            where {
                factor("userId") eq { value(graphics.userId) }
            },
            update {
                set("userId") to graphics.userId
                set("topics") to graphics.topics
            },
            PipelineGraphics::class.java, CollectionNames.PIPELINE_GRAPHICS
        )
    }
}