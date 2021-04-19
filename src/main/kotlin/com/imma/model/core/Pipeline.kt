package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.model.compute.ParameterJointDelegate
import com.imma.persist.annotation.*
import java.util.*

@Suppress("EnumEntryName")
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

@Suppress("EnumEntryName")
enum class PipelineTriggerType(val type: String) {
    insert("insert"),
    merge("merge"),

    // insert or merge
    `insert-or-merge`("insert-or-merge"),
    delete("delete");
}

@Entity(CollectionNames.PIPELINE)
data class Pipeline(
    @Id("_id")
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
    @CreatedAt("_create_time")
    override var createTime: Date? = null,
    @LastModifiedAt("_last_modify_time")
    override var lastModifyTime: Date? = null,
) : Tuple(), Conditional