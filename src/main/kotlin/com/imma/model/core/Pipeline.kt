package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.model.compute.ParameterJoint
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

enum class PipelineStageUnitActionType(val type: String) {
    ALARM("alarm"),
    COPY_TO_MEMORY("copy-to-memory"),
    READ_ROW("read-row"),
    READ_FACTOR("read-factor"),
    EXISTS("exists"),
    MERGE_ROW("merge-row"),
    INSERT_ROW("insert-row"),
    INSERT_OR_MERGE_ROW("insert-or-merge-row"),
    WRITE_FACTOR("write-factor"),
}

data class PipelineStageUnitAction(
    var actionId: String? = null,
    var type: PipelineStageUnitActionType = PipelineStageUnitActionType.ALARM,
)

data class PipelineStageUnit(
    var unitId: String? = null,
    override var conditional: Boolean = false,
    override var on: ParameterJoint = ParameterJoint(),
    var `do`: List<PipelineStageUnitAction> = mutableListOf(),
) : Conditional

data class PipelineStage(
    var stageId: String? = null,
    var name: String? = null,
    override var conditional: Boolean = false,
    override var on: ParameterJoint = ParameterJoint(),
    var units: List<PipelineStageUnit> = mutableListOf(),
) : Conditional

enum class PipelineTriggerType(val type: String) {
    INSERT("insert"),
    MERGE("merge"),

    // insert or merge
    INSERT_OR_MERGE("insert-or-merge"),
    DELETE("delete"),
}

@Document(collection = CollectionNames.PIPELINE)
data class Pipeline(
    @Id
    var pipelineId: String? = null,
    @Field("topic_id")
    var topicId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("type")
    var type: PipelineTriggerType = PipelineTriggerType.INSERT_OR_MERGE,
    @Field("conditional")
    override var conditional: Boolean = false,
    @Field("condition_on")
    override var on: ParameterJoint = ParameterJoint(),
    @Field("stages")
    var stages: MutableList<PipelineStage> = mutableListOf(),
    @Field("enabled")
    var enabled: Boolean = false,
    @Field("validated")
    var validated: Boolean = false,
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple(), Conditional