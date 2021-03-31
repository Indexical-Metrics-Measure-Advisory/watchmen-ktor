package com.imma.service.core

import com.imma.model.core.Pipeline

class Engine {
    companion object {
        fun run(pipeline: Pipeline, data: TriggerData) {
            PipelineWorker(pipeline).use { it.run(data) }
        }
    }
}
