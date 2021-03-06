package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Entity(CollectionNames.SPACE)
data class Space(
	@Id
	var spaceId: String? = null,
	@Field("name")
	var name: String? = null,
	@Field("description")
	var description: String? = null,
	@Field("topic_ids")
	var topicIds: List<String>? = mutableListOf(),
	@CreatedAt
	override var createTime: Date? = null,
	@LastModifiedAt
	override var lastModifyTime: Date? = null,
) : Tuple() {
	// transient fields here for avoid construct exception by spring data using kotlin data class
	@Transient
	var groupIds: List<String>? = mutableListOf()
}