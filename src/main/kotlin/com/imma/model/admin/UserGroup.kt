package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Entity(CollectionNames.USER_GROUP)
data class UserGroup(
	@Id
	var userGroupId: String? = null,
	@Field("name")
	var name: String? = null,
	@Field("description")
	var description: String? = null,
	@Field("space_ids")
	var spaceIds: List<String>? = mutableListOf(),
	@CreatedAt
	override var createTime: Date? = null,
	@LastModifiedAt
	override var lastModifyTime: Date? = null,
) : Tuple() {
	// transient fields here for avoid construct exception by spring data using kotlin data class
	@Transient
	var userIds: List<String>? = mutableListOf()
}
