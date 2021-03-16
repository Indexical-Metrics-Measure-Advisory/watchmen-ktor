package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.model.compute.ParameterJointDelegate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

enum class PipelineStageUnitActionType(val type: String) {
    alarm("alarm"),
    `copy-to-memory`("copy-to-memory"),
    `read-row`("read-row"),
    `read-factor`("read-factor"),
    exists("exists"),
    `merge-row`("merge-row"),
    `insert-row`("insert-row"),
    `insert-or-merge-row`("insert-or-merge-row"),
    `write-factor`("write-factor");
}

data class PipelineStageUnitAction(
    var actionId: String? = null,
    var type: PipelineStageUnitActionType = PipelineStageUnitActionType.alarm,
) : HashMap<String, Any>()

data class PipelineStageUnit(
    var unitId: String? = null,
    override var conditional: Boolean = false,
    override var on: ParameterJointDelegate = mutableMapOf(),
    var `do`: List<PipelineStageUnitAction> = mutableListOf(),
) : Conditional

data class PipelineStage(
    var stageId: String? = null,
    var name: String? = null,
    override var conditional: Boolean = false,
    override var on: ParameterJointDelegate = mutableMapOf(),
    var units: List<PipelineStageUnit> = mutableListOf(),
) : Conditional

enum class PipelineTriggerType(val type: String) {
    insert("insert"),
    merge("merge"),

    // insert or merge
    `insert-or-merge`("insert-or-merge"),
    delete("delete");
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
    var type: PipelineTriggerType = PipelineTriggerType.`insert-or-merge`,
    @Field("conditional")
    override var conditional: Boolean = false,
    @Field("condition_on")
    override var on: ParameterJointDelegate = mutableMapOf(),
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