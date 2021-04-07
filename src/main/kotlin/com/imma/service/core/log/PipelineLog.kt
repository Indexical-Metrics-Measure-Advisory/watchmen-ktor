package com.imma.service.core.log

import com.imma.model.CollectionNames
import com.imma.model.core.PipelineStageUnitActionType
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

interface MonitorActionLog {
    val uid: String
    val type: PipelineStageUnitActionType
    val status: RunStatus
    val completeTime: String
    val error: String?
    val insertCount: Int
    val updateCount: Int
}

data class AlarmActionLog(
    override val uid: String,
    override val type: PipelineStageUnitActionType = PipelineStageUnitActionType.alarm,
    val conditionResult: Boolean,
    val value: String?,
    override val status: RunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int = 0,
    override val updateCount: Int = 0
) : MonitorActionLog

data class ReadActionLog(
    override val uid: String,
    override val type: PipelineStageUnitActionType,
    val value: String?,
    val by: Any,
    override val status: RunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int = 0,
    override val updateCount: Int = 0
) : MonitorActionLog

data class WriteActionLog(
    override val uid: String,
    override val type: PipelineStageUnitActionType,
    val value: String?,
    val by: Any,
    override val status: RunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int,
    override val updateCount: Int
) : MonitorActionLog

data class CopyToMemoryActionLog(
    override val uid: String,
    override val type: PipelineStageUnitActionType = PipelineStageUnitActionType.`copy-to-memory`,
    val value: String?,
    override val status: RunStatus,
    override val completeTime: String,
    override val error: String?,
    override val insertCount: Int = 0,
    override val updateCount: Int = 0
) : MonitorActionLog

data class MonitorUnitLog(
    val uid: String,
    val conditionResult: Boolean,
    val actions: List<MonitorActionLog>,
    val error: String?
)

data class MonitorStageLog(
    val uid: String,
    val conditionResult: Boolean,
    val units: List<MonitorUnitLog>,
    val error: String?
)

@Document(collection = CollectionNames.PIPELINE_LOG)
data class PipelineLog(
    @Field("uid")
    var uid: String,
    @Field("pipeline_id")
    var pipelineId: String,
    @Field("topic_id")
    var topicId: String,
    @Field("status")
    val status: RunStatus = RunStatus.done,
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
    val stages: List<MonitorStageLog>,
    @Field("error")
    val error: String?
)