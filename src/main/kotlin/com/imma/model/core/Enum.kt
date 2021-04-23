package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Entity
data class EnumItem(
    @Id
    var enumItemId: String? = null,
	@Field("code")
	var code: String? = null,
	@Field("label")
	var label: String? = null,
	@Field("replace_code")
	var replaceCode: String? = null,
	@Field("parent_code")
	var parentCode: String? = null,
	@CreatedAt
	var createTime: Date? = null,
	@LastModifiedAt
	var lastModifyTime: Date? = null,
)

@Entity(CollectionNames.ENUM)
data class Enum(
	@Id
	var enumId: String? = null,
	@Field("name")
	var name: String? = null,
	@Field("description")
	var description: String? = null,
	@Field("parent_enum_id")
	var parentEnumId: String? = null,
	@Transient
	var items: MutableList<EnumItem> = mutableListOf(),
	@CreatedAt
	override var createTime: Date? = null,
	@LastModifiedAt
	override var lastModifyTime: Date? = null,
) : Tuple()