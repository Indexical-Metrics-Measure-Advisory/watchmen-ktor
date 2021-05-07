package com.imma.service.core.unit

import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.PipelineStageUnit
import com.imma.service.Services
import com.imma.service.core.EngineLogger
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineTriggerData
import com.imma.service.core.PipelineVariables
import com.imma.service.core.stage.StageContext
import com.imma.utils.neverOccur

open class UnitContext(private val stageContext: StageContext, val unit: PipelineStageUnit) {
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
	open val variables: PipelineVariables
		get() = stageContext.variables

	val services: Services
		get() = stageContext.services
	val logger: EngineLogger
		get() = stageContext.logger

	fun buildLoopContext(delegateVariableName: String, delegateValue: Any?): UnitContext {
		return UnitContextInLoop(stageContext, unit, delegateVariableName, delegateValue)
	}
}

class UnitVariables(
	private val variables: PipelineVariables,
	private val delegateVariableName: String,
	private val delegateValue: Any?
) : PipelineVariables {
	override val size: Int
		get() = variables.size

	override fun containsKey(key: String): Boolean {
		return variables.containsKey(key)
	}

	override fun containsValue(value: Any?): Boolean {
		// assume check value which is delegated, is not gonna happen
		return value == delegateValue || variables.containsValue(value)
	}

	override fun get(key: String): Any? {
		return if (key == delegateVariableName) {
			delegateValue
		} else {
			variables[key]
		}
	}

	override fun isEmpty(): Boolean {
		return variables.isEmpty()
	}

	override val entries: MutableSet<MutableMap.MutableEntry<String, Any?>>
		get() {
			return variables.entries.associate { (key, value) ->
				if (key == delegateVariableName) {
					key to get(delegateVariableName)
				} else {
					key to value
				}
			}.toMutableMap().entries
		}
	override val keys: MutableSet<String>
		get() = variables.keys
	override val values: MutableCollection<Any?>
		get() = this.entries.map { it.value }.toMutableList()

	override fun clear() {
		neverOccur()
	}

	override fun put(key: String, value: Any?): Any? {
		return if (key == delegateVariableName) {
			// replace looped variable is not allowed
			neverOccur()
		} else {
			variables.put(key, value)
		}
	}

	override fun putAll(from: Map<out String, Any?>) {
		neverOccur()
	}

	override fun remove(key: String): Any? {
		neverOccur()
	}
}

class UnitContextInLoop(
	private val stageContext: StageContext,
	unit: PipelineStageUnit,
	private val delegateVariableName: String,
	private val delegateValue: Any?
) :
	UnitContext(stageContext, unit) {
	private val variablesDelegate by lazy {
		UnitVariables(stageContext.variables, delegateVariableName, delegateValue)
	}
	override val variables: PipelineVariables
		get() = variablesDelegate
}