package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Entity(CollectionNames.USER_CREDENTIAL)
data class UserCredential(
	@Id
	var userId: String? = null,
	@Field("name")
	var name: String? = null,
	@Field("credential")
	var credential: String? = null,
	@CreatedAt
	override var createTime: Date? = null,
	@LastModifiedAt
	override var lastModifyTime: Date? = null,
) : Tuple()
