package com.imma.service.core.unit

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.PipelineStageUnit
import com.imma.service.Services
import com.imma.service.core.*
import com.imma.service.core.stage.StageContext

class UnitContext(private val stageContext: StageContext, val unit: PipelineStageUnit) : RunContext {
	val instanceId: String
		get() = stageContext.instanceId
	val pipeline: Pipeline
		get() = stageContext.pipeline
	val stage: PipelineStage
		get() = stageContext.stage
	val topics: PipelineTopics
		get() = stageContext.topics

	val previousOfTriggerData: PipelineTriggerData?
		get() = stageContext.previousOfTriggerData
	val currentOfTriggerData: PipelineTriggerData
		get() = stageContext.currentOfTriggerData
	val variables: PipelineVariables
		get() = stageContext.variables

	val services: Services
		get() = stageContext.services
	val logger: EngineLogger
		get() = stageContext.logger

	override fun isSourceTopic(topicId: String): Boolean {
		return topicId == pipeline.topicId
	}
}