package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

/**
 * DONOT use this entity to save user, it is created for load user list for holder
 */
@Entity(CollectionNames.USER)
class UserForHolder {
    @Id
    var userId: String? = null

    @Field("name")
    var name: String? = null
}