package com.imma.service.core

import com.imma.model.core.Pipeline
import com.imma.service.core.pipeline.PipelineContext
import com.imma.service.core.pipeline.PipelineWorker

data class PipelineTrigger(
	val previous: PipelineTriggerData? = null,
	val now: PipelineTriggerData = mapOf()
)

class Engine {
	companion object {
		fun run(topicId: String, trigger: PipelineTrigger) {
			// TODO find pipelines by given topic id, and trigger them
		}

		fun run(pipeline: Pipeline, trigger: PipelineTrigger) {
			PipelineContext(pipeline, trigger).use { PipelineWorker(it).run() }
		}
	}
}
