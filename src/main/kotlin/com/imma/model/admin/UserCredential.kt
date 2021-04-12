package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Entity(CollectionNames.USER_CREDENTIAL)
data class UserCredential(
    @Id("_id")
    var userId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("credential")
    var credential: String? = null,
    @CreatedAt("create_time")
    override var createTime: Date? = null,
    @LastModifiedAt("last_modify_time")
    override var lastModifyTime: Date? = null,
) : Tuple()
