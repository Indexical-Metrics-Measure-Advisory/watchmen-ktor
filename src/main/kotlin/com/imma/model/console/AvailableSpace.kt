package com.imma.model.console

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * DONOT use this entity to save space, it is created for load available space
 */
@Document(collection = CollectionNames.SPACE)
class AvailableSpace {
    @Id
    var spaceId: String? = null
    var name: String? = null
    var description: String? = null
    var topicIds: List<String>? = mutableListOf()
}