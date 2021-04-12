package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

/**
 * DONOT use this entity to save space, it is created for load space list for holder
 */
@Entity(CollectionNames.SPACE)
class SpaceForHolder {
    @Id("_id")
    var spaceId: String? = null

    @Field("name")
    var name: String? = null
}