package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

/**
 * DONOT use this entity to save enumeration, it is created for load enumeration list for holder
 */
@Entity(CollectionNames.ENUM)
class EnumForHolder {
    @Id
    var enumId: String? = null

    @Field("name")
    var name: String? = null
}