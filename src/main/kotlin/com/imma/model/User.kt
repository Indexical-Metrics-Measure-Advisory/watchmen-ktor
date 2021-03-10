package com.imma.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset.UTC
import java.util.*

@Document(collection = CollectionNames.USER)
data class User(
    @Id
    var userId: String? = null,
    @Indexed(unique = true)
    @Field("name")
    var name: String? = null,
    @Field("nick_name")
    var nickName: String? = null,
    @Field("is_active")
    var active: Boolean = true,
    @Field("role")
    var role: String = "",
    @Field("group_ids")
    var groupIds: List<String>? = mutableListOf(),
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(UTC)).time
) : Tuple() {
    // transient fields here for avoid construct exception by spring data using kotlin data class
    @Transient
    var password: String? = null
}
