package com.imma.service.core

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
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

@Document(collection = CollectionNames.RUN_LOG)
data class RunLog(
    @Id
    var logId: String? = null,
    @Field("instance_id")
    var instanceId: String? = null,
    @Field("pipeline_id")
    var pipelineId: String? = null,
    @Field("run_type")
    var type: PipelineRunType = PipelineRunType.`not-defined`,
    @Field("status")
    var status: PipelineRunStatus = PipelineRunStatus.done,
    @Field("stage_id")
    var stageId: String? = null,
    @Field("unit_id")
    var unitId: String? = null,
    @Field("action_id")
    var actionId: String? = null,
    @Field("message")
    var message: String? = null,
    @Field("create_time")
    var createTime: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
)