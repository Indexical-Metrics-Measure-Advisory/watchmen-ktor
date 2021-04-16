package com.imma.service.core.stage

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.Topic
import com.imma.service.Services
import com.imma.service.core.EngineLogger
import com.imma.service.core.RunContext
import com.imma.service.core.pipeline.PipelineContext

class StageContext(
    private val pipelineContext: PipelineContext,
    val stage: PipelineStage,
    val sourceData: Map<String, Any>
) : RunContext {
    val instanceId: String
        get() {
            return pipelineContext.instanceId
        }
    val pipeline: Pipeline
        get() {
            return pipelineContext.pipeline
        }

    val topics: MutableMap<String, Topic>
        get() {
            return pipelineContext.topics
        }
    val variables: MutableMap<String, Any?> by lazy { mutableMapOf() }

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
