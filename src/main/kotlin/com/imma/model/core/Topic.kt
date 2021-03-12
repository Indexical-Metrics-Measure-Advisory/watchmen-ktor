package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

enum class TopicType(val type: String) {
    SYSTEM("system"),
    RAW("raw"),
    DISTINCT("distinct"),
    AGGREGATE("aggregate"),
    TIME("time"),
    RATIO("ratio")
}

@Document(collection = CollectionNames.TOPIC)
data class Topic(
    @Id
    var topicId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("type")
    var type: TopicType = TopicType.DISTINCT,
    @Field("description")
    var description: String? = null,
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple() {
    @Transient
    var factors: List<Factor> = mutableListOf()
}