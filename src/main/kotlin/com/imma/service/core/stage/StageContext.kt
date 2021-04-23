package com.imma.service.core.stage

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.service.Services
import com.imma.service.core.*
import com.imma.service.core.pipeline.PipelineContext

class StageContext(
	private val pipelineContext: PipelineContext,
	val stage: PipelineStage
) : RunContext {
	val instanceId: String
		get() = pipelineContext.instanceId
	val pipeline: Pipeline
		get() = pipelineContext.pipeline
	val topics: PipelineTopics
		get() = pipelineContext.topics

	val previousOfTriggerData: PipelineTriggerData?
		get() = pipelineContext.previousOfTriggerData
	val currentOfTriggerData: PipelineTriggerData
		get() = pipelineContext.currentOfTriggerData
	val variables: PipelineVariables
		get() = pipelineContext.variables

	val services: Services
		get() = pipelineContext.services
	val logger: EngineLogger
		get() = pipelineContext.logger

	override fun isSourceTopic(topicId: String): Boolean {
		return topicId == pipeline.topicId
	}
}
