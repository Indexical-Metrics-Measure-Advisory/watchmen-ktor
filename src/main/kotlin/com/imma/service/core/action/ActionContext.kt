package com.imma.service.core.action

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.PipelineStageUnit
import com.imma.model.core.PipelineStageUnitAction
import com.imma.service.Services
import com.imma.service.core.EngineLogger
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineTriggerData
import com.imma.service.core.PipelineVariables
import com.imma.service.core.unit.UnitContext

class ActionContext(private val unitContext: UnitContext, val action: PipelineStageUnitAction) {
	val instanceId: String
		get() = unitContext.instanceId
	val pipeline: Pipeline
		get() = unitContext.pipeline
	val stage: PipelineStage
		get() = unitContext.stage
	val unit: PipelineStageUnit
		get() = unitContext.unit
	val topics: PipelineTopics
		get() = unitContext.topics

	val previousOfTriggerData: PipelineTriggerData?
		get() = unitContext.previousOfTriggerData
	val currentOfTriggerData: PipelineTriggerData
		get() = unitContext.currentOfTriggerData
	val variables: PipelineVariables
		get() = unitContext.variables

	val services: Services
		get() = unitContext.services
	val logger: EngineLogger
		get() = unitContext.logger
}
