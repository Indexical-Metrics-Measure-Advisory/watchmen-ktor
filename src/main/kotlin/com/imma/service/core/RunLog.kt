package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.PipelineStageUnitActionType
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

@Suppress("EnumEntryName")
enum class PipelineRunType(val type: String) {
    invalidate("invalidate"),
    disable("disable"),
    ignore("ignore"),
    fail("fail"),

    start("start"),
    end("end"),

    `not-defined`("not-defined")
}

@Suppress("EnumEntryName")
enum class PipelineRunStatus(val type: String) {
    done("done"),
    error("error")
}

data class RunLog(
    var logId: String? = null,
    var instanceId: String? = null,
    var pipelineId: String? = null,
    var type: PipelineRunType = PipelineRunType.`not-defined`,
    var status: PipelineRunStatus = PipelineRunStatus.done,
    var stageId: String? = null,
    var unitId: String? = null,
    var actionId: String? = null,
    var message: String? = null,
    var error: String? = null,
    var oldValue: Map<String, Any>? = null,
    var newValue: Map<String, Any>? = null,
    var createTime: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time,
    var completeTime: Double? = null,
    var insertCount: Int? = null,
    var updateCount: Int? = null
)

interface MonitorLogAction {
    val uid: String
    val type: PipelineStageUnitActionType
    val status: PipelineRunStatus
    val completeTime: String
    val error: String?
    val insertCount: Int
    val updateCount: Int
}

data class AlarmAction(
    override val uid: String,
    override val type: PipelineStageUnitActionType = PipelineStageUnitActionType.alarm,
    val conditionResult: Boolean,
    val value: String?,
    override val status: PipelineRunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int = 0,
    override val updateCount: Int = 0
) : MonitorLogAction

data class ReadAction(
    override val uid: String,
    override val type: PipelineStageUnitActionType,
    val value: String?,
    val by: Any,
    override val status: PipelineRunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int = 0,
    override val updateCount: Int = 0
) : MonitorLogAction

data class WriteAction(
    override val uid: String,
    override val type: PipelineStageUnitActionType,
    val value: String?,
    val by: Any,
    override val status: PipelineRunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int,
    override val updateCount: Int
) : MonitorLogAction

data class CopyToMemoryAction(
    override val uid: String,
    override val type: PipelineStageUnitActionType = PipelineStageUnitActionType.`copy-to-memory`,
    val value: String?,
    override val status: PipelineRunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int = 0,
    override val updateCount: Int = 0
) : MonitorLogAction

data class MonitorLogUnit(
    val uid: String,
    val conditionResult: Boolean,
    val actions: List<MonitorLogAction>,
    val error: String?
)

data class MonitorLogStage(
    val uid: String,
    val conditionResult: Boolean,
    val units: List<MonitorLogUnit>,
    val error: String?
)

@Document(collection = CollectionNames.RUN_LOG)
data class MonitorLogs(
    @Field("uid")
    var uid: String,
    @Field("pipeline_id")
    var pipelineId: String,
    @Field("topic_id")
    var topicId: String,
    @Field("status")
    val status: PipelineRunStatus = PipelineRunStatus.done,
    @Field("start_time")
    val startTime: Date,
    @Field("complete_time")
    val completeTime: Double,
    @Field("old_value")
    val oldValue: Map<String, Any>,
    @Field("new_value")
    val newValue: Map<String, Any>,
    @Field("condition_result")
    val conditionResult: Boolean,
    @Field("stages_log")
    val stages: List<MonitorLogStage>,
    @Field("error")
    val error: String?
)