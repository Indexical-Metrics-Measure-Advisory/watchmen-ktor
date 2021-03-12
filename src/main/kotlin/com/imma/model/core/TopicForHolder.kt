package com.imma.model.core

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * DONOT use this entity to save topic, it is created for load topic list for holder
 */
@Document(collection = CollectionNames.TOPIC)
class TopicForHolder {
    @Id
    var topicId: String? = null
    var name: String? = null
}