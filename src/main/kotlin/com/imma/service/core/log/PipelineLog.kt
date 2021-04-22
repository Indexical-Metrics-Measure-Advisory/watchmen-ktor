package com.imma.service.core.log

import com.imma.model.CollectionNames
import com.imma.model.core.PipelineStageUnitActionType
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id
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
