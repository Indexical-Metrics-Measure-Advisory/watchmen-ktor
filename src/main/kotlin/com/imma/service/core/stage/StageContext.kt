package com.imma.service.core.stage

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.service.Services
import com.imma.service.core.*
import com.imma.service.core.pipeline.PipelineContext

class StageContext(
    private val pipelineContext: PipelineContext,
    val stage: PipelineStage,
    val sourceData: PipelineSourceData
) : RunContext {
    val instanceId: String
        get() {
            return pipelineContext.instanceId
        }
    val pipeline: Pipeline
        get() {
            return pipelineContext.pipeline
        }

    val topics: PipelineTopics
        get() {
            return pipelineContext.topics
        }
    val variables: PipelineVariables by lazy { createPipelineVariables() }

    val services: Services
        get() {
            return pipelineContext.services
        }
    val logger: EngineLogger
        get() {
            return pipelineContext.logger
        }

    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }
}
