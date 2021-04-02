package com.imma.service.core

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.Topic
import com.imma.service.Services

class StageWorkerContextBuilder {
    var instanceId: String? = null
    var pipeline: Pipeline? = null
    var stage: PipelineStage? = null
    var topics: MutableMap<String, Topic>? = null
    var sourceData: Map<String, Any>? = null
    var variables: Map<String, Any>? = null

    var services: Services? = null
    var logger: LoggerWorker? = null

    fun build(): StageWorkerContext {
        return StageWorkerContext(
            instanceId ?: throw RuntimeException("Instance id cannot be null."),
            pipeline ?: throw RuntimeException("Pipeline cannot be null."),
            stage ?: throw RuntimeException("Stage cannot be null."),
            topics ?: throw RuntimeException("Topics cannot be null."),
            sourceData ?: mutableMapOf(),
            variables ?: mutableMapOf(),
            services ?: throw RuntimeException("Services cannot be null."),
            logger ?: throw RuntimeException("Logger worker cannot be null."),
        )
    }
}

data class StageWorkerContext(
    val instanceId: String,
    val pipeline: Pipeline,
    val stage: PipelineStage,
    var topics: MutableMap<String, Topic>,
    var sourceData: Map<String, Any>,
    var variables: Map<String, Any>,

    var services: Services,
    var logger: LoggerWorker
)

class StageWorker(context: StageWorkerContext) {
    fun run() {
        // TODO run stage
    }
}