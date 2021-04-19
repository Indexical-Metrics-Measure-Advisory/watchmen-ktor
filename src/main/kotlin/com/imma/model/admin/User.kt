package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Entity(CollectionNames.USER)
data class User(
    @Id("_id")
    var userId: String? = null,
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
    @CreatedAt("_create_time")
    override var createTime: Date? = null,
    @LastModifiedAt("_last_modify_time")
    override var lastModifyTime: Date? = null,
) : Tuple() {
    // transient fields here for avoid construct exception by spring data using kotlin data class
    @Transient
    var password: String? = null
}
