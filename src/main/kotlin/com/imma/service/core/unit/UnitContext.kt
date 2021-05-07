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

	fun buildLoopContext(delegatedVariableName: String, delegatedValueIndex: Int): UnitContext {
		return UnitContextInLoop(stageContext, unit, delegatedVariableName, delegatedValueIndex)
	}
}

class UnitVariables(
	private val variables: PipelineVariables,
	private val delegatedVariableName: String,
	private val delegatedValueIndex: Int
) : PipelineVariables {
	override val size: Int
		get() = variables.size

	override fun containsKey(key: String): Boolean {
		return variables.containsKey(key)
	}

	override fun containsValue(value: Any?): Boolean {
		return variables.containsValue(value)
	}

	override fun get(key: String): Any? {
		return if (key == delegatedVariableName) {
			when (val values = variables[key]) {
				is Collection<Any?> -> values.toTypedArray()[delegatedValueIndex]
				is Array<*> -> values[delegatedValueIndex]
				else -> throw RuntimeException("Cannot get value from $values on index[$delegatedValueIndex].")
			}
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
				if (key == delegatedVariableName) {
					key to get(delegatedVariableName)
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
		variables.clear()
	}

	@Suppress("UNCHECKED_CAST")
	override fun put(key: String, value: Any?): Any? {
		return if (key == delegatedVariableName) {
			when (val values = variables[key]) {
				is MutableList<*> -> {
					val old = values[delegatedValueIndex]
					(values as MutableList<Any?>)[delegatedValueIndex] = value
					old
				}
				is Array<*> -> {
					val old = values[delegatedValueIndex]
					(values as Array<Any?>)[delegatedValueIndex] = value
					old
				}
				else -> throw RuntimeException("Cannot put value to $values on index[$delegatedValueIndex].")
			}
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
	private val delegatedVariableName: String,
	private val delegatedValueIndex: Int
) :
	UnitContext(stageContext, unit) {
	private val variablesDelegate by lazy {
		UnitVariables(stageContext.variables, delegatedVariableName, delegatedValueIndex)
	}
	override val variables: PipelineVariables
		get() = variablesDelegate
}