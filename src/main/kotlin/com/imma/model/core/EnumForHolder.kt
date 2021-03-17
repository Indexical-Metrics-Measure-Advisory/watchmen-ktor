package com.imma.model.core

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * DONOT use this entity to save enumeration, it is created for load enumeration list for holder
 */
@Document(collection = CollectionNames.ENUM)
class EnumForHolder {
    @Id
    var topicId: String? = null
    var name: String? = null
}