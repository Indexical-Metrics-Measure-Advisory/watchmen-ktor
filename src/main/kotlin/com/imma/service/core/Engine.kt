package com.imma.service.core

import com.imma.model.core.Pipeline
import com.imma.service.core.pipeline.PipelineContext
import com.imma.service.core.pipeline.PipelineWorker

data class TriggerData(
    val topicId: String,
    val previous: Map<String, Any> = mapOf(),
    val now: Map<String, Any> = mapOf()
)

class Engine {
    companion object {
        fun run(pipeline: Pipeline, data: TriggerData) {
            PipelineContext(pipeline).use { PipelineWorker(it).run(data) }
        }
    }
}
