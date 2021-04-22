package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.persist.annotation.*
import com.imma.service.core.log.MonitorStageLog
import com.imma.service.core.log.RunStatus
import java.util.*

@Entity(CollectionNames.PIPELINE_LOG)
data class PipelineLog(
	@Id("_id")
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
	val error: String?,
	@CreatedAt("_create_time")
	var createTime: Date? = null,
	@LastModifiedAt("_last_modify_time")
	var lastModifyTime: Date? = null,
)