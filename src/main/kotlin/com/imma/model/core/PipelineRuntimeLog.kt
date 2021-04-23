package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.persist.annotation.*
import java.util.*

@Suppress("EnumEntryName")
enum class RunStatus(var type: String) {
	done("done"),
	error("error")
}

interface StandardMonitorLog {
	var uid: String?
	var status: RunStatus
	var startTime: Date?
	var completeTime: Double
	var message: String?
	var error: String?
}

interface LogHasConditional : StandardMonitorLog {
	var conditionResult: Boolean
}

interface ActionLog : StandardMonitorLog {
	var actionId: String?
	var type: PipelineStageUnitActionType
	var insertCount: Int
	var updateCount: Int
}

data class AlarmActionLog(
	override var uid: String? = null,
	override var actionId: String? = null,
	override var type: PipelineStageUnitActionType = PipelineStageUnitActionType.alarm,
	override var conditionResult: Boolean = false,
	var value: Any? = null,
	override var status: RunStatus = RunStatus.done,
	override var startTime: Date? = null,
	override var completeTime: Double = 0.0,
	override var message: String? = null,
	override var error: String? = null,
	override var insertCount: Int = 0,
	override var updateCount: Int = 0
) : ActionLog, LogHasConditional

data class CopyToMemoryActionLog(
	override var uid: String? = null,
	override var actionId: String? = null,
	override var type: PipelineStageUnitActionType = PipelineStageUnitActionType.`copy-to-memory`,
	var value: Any? = null,
	override var status: RunStatus = RunStatus.done,
	override var startTime: Date? = null,
	override var completeTime: Double = 0.0,
	override var message: String? = null,
	override var error: String? = null,
	override var insertCount: Int = 0,
	override var updateCount: Int = 0
) : ActionLog

data class ReadActionLog(
	override var uid: String? = null,
	override var actionId: String? = null,
	override var type: PipelineStageUnitActionType,
	var value: Any? = null,
	var by: Any? = null,
	override var status: RunStatus = RunStatus.done,
	override var startTime: Date? = null,
	override var completeTime: Double = 0.0,
	override var message: String? = null,
	override var error: String? = null,
	override var insertCount: Int = 0,
	override var updateCount: Int = 0
) : ActionLog

data class WriteActionLog(
	override var uid: String? = null,
	override var actionId: String? = null,
	override var type: PipelineStageUnitActionType,
	var oldValue: Any? = null,
	var newValue: Any? = null,
	var by: Any? = null,
	override var status: RunStatus = RunStatus.done,
	override var startTime: Date? = null,
	override var completeTime: Double = 0.0,
	override var message: String? = null,
	override var error: String? = null,
	override var insertCount: Int = 0,
	override var updateCount: Int = 0
) : ActionLog

data class UnitLog(
	override var uid: String? = null,
	var unitId: String? = null,
	override var status: RunStatus = RunStatus.done,
	override var startTime: Date? = null,
	override var completeTime: Double = 0.0,
	override var conditionResult: Boolean = false,
	var actions: MutableList<ActionLog> = mutableListOf(),
	override var message: String? = null,
	override var error: String? = null
) : LogHasConditional

data class StageLog(
	override var uid: String? = null,
	var stageId: String? = null,
	override var status: RunStatus = RunStatus.done,
	override var startTime: Date? = null,
	override var completeTime: Double = 0.0,
	override var conditionResult: Boolean = false,
	var units: MutableList<UnitLog> = mutableListOf(),
	override var message: String? = null,
	override var error: String? = null
) : LogHasConditional

@Entity(CollectionNames.RUNTIME_PIPELINE_LOG)
data class PipelineRuntimeLog(
	@Id
	override var uid: String? = null,
	@Field("pipeline_id")
	var pipelineId: String? = null,
	@Field("topic_id")
	var topicId: String? = null,
	@Field("status")
	override var status: RunStatus = RunStatus.done,
	@Field("start_time")
	override var startTime: Date? = null,
	@Field("complete_time")
	override var completeTime: Double = 0.0,
	@Field("old_value")
	var oldValue: Map<String, Any?>? = mapOf(),
	@Field("new_value")
	var newValue: Map<String, Any?> = mapOf(),
	@Field("condition_result")
	override var conditionResult: Boolean = false,
	@Field("stages")
	var stages: MutableList<StageLog> = mutableListOf(),
	@Field("message")
	override var message: String? = null,
	@Field("error")
	override var error: String? = null,
	@CreatedAt
	var createTime: Date? = null,
	@LastModifiedAt
	var lastModifyTime: Date? = null,
) : LogHasConditional