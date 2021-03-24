package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.PipelineGraphics
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class PipelineGraphicsService(application: Application) : Service(application) {
    fun findPipelineGraphicsById(userId: String): PipelineGraphics? {
        return persistKit.findById(userId, PipelineGraphics::class.java, CollectionNames.PIPELINE_GRAPHICS)
    }

    fun savePipelineGraphicsByUser(graphics: PipelineGraphics) {
        writeIntoMongo {
            it.upsert(
                Query.query(Criteria.where("userId").`is`(graphics.userId)),
                Update().apply {
                    set("userId", graphics.userId)
                    set("topics", graphics.topics)
                },
                PipelineGraphics::class.java,
                CollectionNames.PIPELINE_GRAPHICS
            )
        }
    }
}