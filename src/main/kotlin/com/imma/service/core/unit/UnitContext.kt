package com.imma.service.core.unit

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.PipelineStageUnit
import com.imma.model.core.Topic
import com.imma.service.Services
import com.imma.service.core.*
import com.imma.service.core.stage.StageContext

class UnitContext(private val stageContext: StageContext, val unit: PipelineStageUnit) : RunContext {
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

    val topics: PipelineTopics
        get() {
            return stageContext.topics
        }
    val sourceData: PipelineSourceData
        get() {
            return stageContext.sourceData
        }
    val variables: PipelineVariables
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

    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }
}