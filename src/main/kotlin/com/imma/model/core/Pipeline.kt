package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.model.compute.ParameterJoint
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

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
    @Transient
    var stages: List<PipelineStage> = mutableListOf(),
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