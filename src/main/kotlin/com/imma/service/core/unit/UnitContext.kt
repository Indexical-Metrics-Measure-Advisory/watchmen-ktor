package com.imma.service.core.unit

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.PipelineStageUnit
import com.imma.model.core.Topic
import com.imma.service.Services
import com.imma.service.core.EngineLogger
import com.imma.service.core.stage.StageContext

class UnitContext(private val stageContext: StageContext, val unit: PipelineStageUnit) {
    val instanceId: String
        get() {
            return stageContext.instanceId
        }
    val pipeline: Pipeline
        get() {
            return stageContext.pipeline
        }
    val stage: PipelineStage
        get() {
            return stageContext.stage
        }

    val topics: MutableMap<String, Topic>
        get() {
            return stageContext.topics
        }
    val sourceData: Map<String, Any>
        get() {
            return stageContext.sourceData
        }
    val variables: MutableMap<String, Any>
        get() {
            return stageContext.variables
        }

    val services: Services
        get() {
            return stageContext.services
        }
    val logger: EngineLogger
        get() {
            return stageContext.logger
        }
}