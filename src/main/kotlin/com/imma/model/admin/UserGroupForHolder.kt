package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

/**
 * DONOT use this entity to save user group, it is created for load user group list for holder
 */
@Entity(CollectionNames.USER_GROUP)
class UserGroupForHolder {
    @Id
    var userGroupId: String? = null

    @Field("name")
    var name: String? = null
}