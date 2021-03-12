package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
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

@Document(collection = CollectionNames.PIPELINE_STAGE_UNIT_ACTION)
data class PipelineStageUnitAction(
    @Id
    var actionId: String? = null,
    @Field("type")
    var type: PipelineStageUnitActionType = PipelineStageUnitActionType.ALARM,
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple()