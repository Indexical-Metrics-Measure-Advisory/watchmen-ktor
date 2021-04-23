package com.imma.service.core

import com.imma.model.EntityColumns
import com.imma.model.core.*
import com.imma.service.Service
import com.imma.service.Services
import com.imma.service.core.log.RunLog
import com.imma.service.core.log.RunType
import com.imma.service.core.pipeline.PipelineContext
import com.imma.utils.neverOccur
import org.slf4j.LoggerFactory
import java.io.Closeable

class EngineLogger(private val instanceId: String, services: Services) : Service(services), Closeable {
	private val logs: MutableList<RunLog> = mutableListOf()

	internal fun append(block: RunLog.() -> Unit) {
		val log = RunLog(instanceId).apply(block)
		logs += log
	}

	private fun StandardMonitorLog.lastWin(
		log: RunLog,
		onInvalidate: () -> Unit = neverOccur(),
		onDisable: () -> Unit = neverOccur()
	) {
		message = log.message
		completeTime = log.completeTime ?: 0.0
		when (log.type) {
			// ignored caused by condition check
			RunType.ignore -> {
				status = RunStatus.done
				if (this is LogHasConditional) {
					conditionResult = false
				}
			}
			// pipeline started, which means condition result is true
			RunType.start -> if (this is LogHasConditional) {
				conditionResult = true
			}
			// fail end
			RunType.fail -> {
				status = RunStatus.error
				error = log.error
			}
			// success end
			RunType.end -> status = RunStatus.done

			RunType.invalidate -> onInvalidate()
			RunType.disable -> onDisable()
			RunType.process -> neverOccur()
			RunType.`not-defined` -> neverOccur()
		}
	}

	private fun PipelineContext.writePipelineLog(
		log: RunLog,
		pipelineRuntimeLog: PipelineRuntimeLog?
	): PipelineRuntimeLog {
		return pipelineRuntimeLog.run {
			// first time, create and fill the following properties
			this ?: PipelineRuntimeLog(
				uid = log.instanceId,
				pipelineId = log.pipelineId,
				topicId = pipeline.topicId,
				startTime = log.createTime,
				oldValue = previousOfTriggerData,
				newValue = currentOfTriggerData
			)
		}.also {
			it.lastWin(log, onInvalidate = { it.status = RunStatus.done }, onDisable = { it.status = RunStatus.done })
		}
	}

	private fun writeStageLog(log: RunLog, stageLog: StageLog?): StageLog {
		return when {
			// first time
			// or given stage log is for previous stage
			// create stage log object and fill the following properties
			stageLog == null || stageLog.stageId != log.stageId -> StageLog(
				uid = log.instanceId,
				stageId = log.stageId,
				startTime = log.createTime,
			)
			else -> stageLog
		}.also { it.lastWin(log) }
	}

	private fun writeUnitLog(log: RunLog, unitLog: UnitLog?): UnitLog {
		return when {
			// first time
			// or given unit log is for previous stage
			// create unit log object and fill the following properties
			unitLog == null || unitLog.unitId != log.unitId -> UnitLog(
				uid = log.instanceId,
				unitId = log.unitId,
				startTime = log.createTime,
			)
			else -> unitLog
		}.also { it.lastWin(log) }
	}

	private fun writeActionLog(log: RunLog, actionLog: ActionLog?): Pair<String?, ActionLog> {
		return when {
			// first time
			// or given unit log is for previous stage
			// create unit log object and fill the following properties
			actionLog == null || actionLog.actionId != log.actionId -> when (val actionType = log.actionType) {
				PipelineStageUnitActionType.alarm -> AlarmActionLog(type = actionType)
				PipelineStageUnitActionType.`copy-to-memory` -> CopyToMemoryActionLog(type = actionType)
				PipelineStageUnitActionType.exists -> ReadActionLog(type = actionType)
				PipelineStageUnitActionType.`read-row` -> ReadActionLog(type = actionType)
				PipelineStageUnitActionType.`read-factor` -> ReadActionLog(type = actionType)
				PipelineStageUnitActionType.`insert-row` -> WriteActionLog(type = actionType)
				PipelineStageUnitActionType.`merge-row` -> WriteActionLog(type = actionType)
				PipelineStageUnitActionType.`insert-or-merge-row` -> WriteActionLog(type = actionType)
				PipelineStageUnitActionType.`write-factor` -> WriteActionLog(type = actionType)
				else -> neverOccur()
			}.also {
				it.uid = log.instanceId
				it.startTime = log.createTime
			}
			else -> actionLog
		}.let {
			var topicId: String? = null
			when (it) {
				is AlarmActionLog -> it.value = log.data?.get("value")
				is CopyToMemoryActionLog -> it.value = log.data?.get("value")
				is ReadActionLog -> it.value = log.data?.get("value")
				is WriteActionLog -> {
					topicId = log.data?.get("topicId")?.toString()
					it.oldValue = log.data?.get("oldValue")
					it.newValue = log.data?.get("newValue")
					it.insertCount = (log.data?.get("insertCount") ?: 0).toString().toInt()
					it.updateCount = (log.data?.get("updateCount") ?: 0).toString().toInt()
				}
				else -> neverOccur()
			}
			topicId to it
		}.also {
			it.second.lastWin(log)
		}
	}

	/**
	 * @return a list of data changes.
	 *      first: topicId
	 *      second: oldValue
	 *      third: newValue
	 *    note even for same topic, multiple records are possible. but for same topic and same id, only one record exists in list.
	 */
	fun output(context: PipelineContext): List<Triple<String, PipelineTriggerData?, PipelineTriggerData>> {
		val changes = mutableListOf<Triple<String, Any?, Any?>>()
		try {
			var pipelineRuntimeLog: PipelineRuntimeLog? = null
			var stageLog: StageLog? = null
			var unitLog: UnitLog? = null
			var actionLog: ActionLog? = null

			for (log in logs) {
				when {
					!log.actionId.isNullOrEmpty() -> writeActionLog(log, actionLog).also { (topicId, thisLog) ->
						actionLog = thisLog
						if (!unitLog!!.actions.contains(thisLog)) {
							unitLog!!.actions.add(thisLog)
						}
						// gather changes
						if (!topicId.isNullOrEmpty() && thisLog is WriteActionLog) {
							changes.add(Triple(topicId, thisLog.oldValue, thisLog.newValue))
						}
					}
					!log.unitId.isNullOrEmpty() -> writeUnitLog(log, unitLog).also { thisLog ->
						unitLog = thisLog
						if (!stageLog!!.units.contains(thisLog)) {
							stageLog!!.units.add(thisLog)
						}
					}
					!log.stageId.isNullOrEmpty() -> writeStageLog(log, stageLog).also { thisLog ->
						stageLog = thisLog
						if (!pipelineRuntimeLog!!.stages.contains(thisLog)) {
							pipelineRuntimeLog!!.stages.add(thisLog)
						}
					}
					else -> pipelineRuntimeLog = context.writePipelineLog(log, pipelineRuntimeLog)
				}
			}

			// persist pipeline log
			services.dynamicTopic { insertRuntimePipelineLog(pipelineRuntimeLog!!) }
		} catch (t: Throwable) {
			LoggerFactory.getLogger(javaClass).error("Error occurs on output runtime pipeline log.", t)
			// never rethrow it
		}

		// return data changed on topics, for trigger next pipelines
		return changes.fold(mutableMapOf<String, MutableList<Pair<Any?, Any?>>>()) { map, (topicId, oldValue, newValue) ->
			// new value always exists, and it is always be a map with "_id"
			val dataId = (newValue!! as Map<*, *>)[EntityColumns.OBJECT_ID]!!
			// mix topic id and data id, as key
			val key = "$topicId,$dataId"
			val diffs = map[key]
			if (diffs == null) {
				map[key] = mutableListOf(oldValue to newValue)
			} else {
				diffs.add(oldValue to newValue)
			}
			map
		}.map { (key, diffs) ->
			val topicId = key.split(",")[0]
			@Suppress("UNCHECKED_CAST")
			Triple(
				topicId,
				diffs[0].first as PipelineTriggerData?,
				diffs[diffs.size - 1].second as PipelineTriggerData
			)
		}
	}

	override fun close() {
		services.close()
	}
}

abstract class EngineLoggerDelegate(protected val logger: EngineLogger) {
	abstract fun fillIds(log: RunLog)

	fun ignore(msg: String) {
		logger.append {
			fillIds(this)
			message = msg
			type = RunType.ignore
			completeTime = 0.toDouble()
		}
	}

	fun start(msg: String) {
		logger.append {
			fillIds(this)
			message = msg
			type = RunType.start
		}
	}

	fun process(vararg pairs: Pair<String, Any?>) {
		logger.append {
			fillIds(this)
			data = mutableMapOf(*pairs)
			type = RunType.process
		}
	}

	fun success(msg: String, spent: Double) {
		logger.append {
			fillIds(this)
			message = msg
			type = RunType.end
			completeTime = spent
		}
	}

	fun fail(msg: String, t: Throwable, spent: Double) {
		logger.append {
			fillIds(this)
			error = "$msg\nCaused by ${t.stackTraceToString()}."
			type = RunType.fail
			status = RunStatus.error
			completeTime = spent
		}
	}
}
