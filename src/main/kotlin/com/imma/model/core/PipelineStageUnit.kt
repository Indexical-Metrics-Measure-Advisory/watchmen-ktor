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

@Document(collection = CollectionNames.PIPELINE_STAGE_UNIT)
data class PipelineStageUnit(
    @Id
    var unitId: String? = null,
    @Field("conditional")
    override var conditional: Boolean = false,
    @Field("condition_on")
    override var on: ParameterJoint = ParameterJoint(),
    @Transient
    var `do`: List<PipelineStageUnitAction> = mutableListOf(),
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple(), Conditional