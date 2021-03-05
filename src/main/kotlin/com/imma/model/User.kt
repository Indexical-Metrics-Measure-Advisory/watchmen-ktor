package com.imma.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset.UTC
import java.util.*

@Document(collection = "user")
data class User(
    @Id
    var userId: String? = null,
    @Indexed(unique = true)
    @Field("name")
    var name: String? = null,
    @Field("nick_name")
    var nickName: String? = null,
    @Field("password")
    var password: String? = null,
    @Field("is_active")
    var active: Boolean = true,
    @Field("role")
    var role: String = "",
    @Field("user_group_ids")
    var userGroupIds: List<String>? = mutableListOf(),
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(UTC)).time
) : Tuple()
