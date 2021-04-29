package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.core.PipelineGraphics
import com.imma.service.Service
import com.imma.service.Services

class PipelineGraphicsService(services: Services) : Service(services) {
	fun findPipelineGraphicsById(userId: String): PipelineGraphics? {
		return persist().findById(userId, PipelineGraphics::class.java, CollectionNames.PIPELINE_GRAPHICS)
	}

	fun savePipelineGraphicsByUser(graphics: PipelineGraphics) {
		persist().upsertOne(graphics, PipelineGraphics::class.java, CollectionNames.PIPELINE_GRAPHICS)
	}
}