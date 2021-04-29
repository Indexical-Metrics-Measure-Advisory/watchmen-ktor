package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

/**
 * DONOT use this entity to save topic, it is created for load topic list for holder
 */
@Entity(CollectionNames.TOPIC)
class TopicForHolder {
	@Id
	var topicId: String? = null

	@Field("name")
	var name: String? = null
}