package com.imma.service.core.log

import com.imma.model.core.PipelineStageUnitActionType
import com.imma.model.core.RunStatus
import java.time.ZoneId
import java.util.*

data class RunLog(
	var logId: String? = null,
	var instanceId: String? = null,
	var pipelineId: String? = null,
	var type: RunType = RunType.`not-defined`,
	var status: RunStatus = RunStatus.done,
	var stageId: String? = null,
	var unitId: String? = null,
	var actionId: String? = null,
	var actionType: PipelineStageUnitActionType? = null,
	var message: String? = null,
	var error: String? = null,
	var data: Map<String, Any?>? = null,
	var createTime: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault())).time,
	var completeTime: Double? = null,
	var insertCount: Int? = null,
	var updateCount: Int? = null
)
