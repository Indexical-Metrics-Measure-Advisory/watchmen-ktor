package com.imma.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * DONOT use this entity to save space, it is created for load space list for holder
 */
@Document(collection = CollectionNames.SPACE)
class SpaceForHolder {
    @Id
    var spaceId: String? = null
    var name: String? = null
}