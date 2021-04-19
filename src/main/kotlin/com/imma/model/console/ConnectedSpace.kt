package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Entity(CollectionNames.CONNECTED_SPACE)
data class ConnectedSpace(
    @Id("_id")
    var connectId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("space_id")
    var spaceId: String? = null,
    @Field("user_id")
    var userId: String? = null,
    @Field("last_visit_time")
    var lastVisitTime: String? = null,
    @CreatedAt("_create_time")
    override var createTime: Date? = null,
    @LastModifiedAt("_last_modify_time")
    override var lastModifyTime: Date? = null,
) : Tuple() {
    @Transient
    var subjects: MutableList<Subject> = mutableListOf()
}