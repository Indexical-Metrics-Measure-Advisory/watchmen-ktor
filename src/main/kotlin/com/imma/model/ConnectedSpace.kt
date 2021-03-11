package com.imma.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

@Document(collection = CollectionNames.CONNECTED_SPACE)
data class ConnectedSpace(
    @Id
    var connectId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("space_id")
    var spaceId: String? = null,
    @Field("user_id")
    var userId: String? = null,
    @Field("last_visit_time")
    var lastVisitTime: String? = null,
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple() {
    @Transient
    var subjects: MutableList<Subject> = mutableListOf()
}