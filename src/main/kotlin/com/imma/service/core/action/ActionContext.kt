package com.imma.service.core.action

import com.imma.model.core.*
import com.imma.service.Services
import com.imma.service.core.*
import com.imma.service.core.unit.UnitContext

class ActionContext(private val unitContext: UnitContext, val action: PipelineStageUnitAction) : RunContext {
    val instanceId: String
        get() {
            return unitContext.instanceId
        }
    val pipeline: Pipeline
        get() {
            return unitContext.pipeline
        }
    val stage: PipelineStage
        get() {
            return unitContext.stage
        }
    val unit: PipelineStageUnit
        get() {
            return unitContext.unit
        }

    val topics: PipelineTopics
        get() {
            return unitContext.topics
        }
    val sourceData: PipelineSourceData
        get() {
            return unitContext.sourceData
        }
    val variables: PipelineVariables
        get() {
            return unitContext.variables
        }

    val services: Services
        get() {
            return unitContext.services
        }
    val logger: EngineLogger
        get() {
            return unitContext.logger
        }

    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }
}
