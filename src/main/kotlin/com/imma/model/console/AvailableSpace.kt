package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

/**
 * DONOT use this entity to save space, it is created for load available space
 */
@Entity(CollectionNames.SPACE)
class AvailableSpace {
	@Id
	var spaceId: String? = null

	@Field("name")
	var name: String? = null

	@Field("description")
	var description: String? = null

	@Field("topic_ids")
	var topicIds: List<String>? = mutableListOf()
}