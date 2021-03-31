package com.imma.service.core

import com.imma.model.core.Pipeline
import com.imma.service.Services

class Engine {
    private val services: Services by lazy { Services() }
    private val engineLogger: EngineLogger by lazy { EngineLogger(services) }

    fun run(pipeline: Pipeline, data: TriggerData) {
        when {
            !pipeline.validated -> engineLogger.log("Pipeline[${pipeline.pipelineId}] is invalidated.")
            !pipeline.enabled -> engineLogger.log("Pipeline[${pipeline.pipelineId}] is not enabled.")
            else -> doRun(pipeline, data)
        }
    }

    private fun doRun(pipeline: Pipeline, data: TriggerData) {

    }
}