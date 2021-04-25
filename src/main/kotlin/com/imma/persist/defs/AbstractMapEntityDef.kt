package com.imma.persist.defs

import com.imma.model.EntityColumns
import com.imma.persist.DynamicTopicKits

class MapEntityFieldDef(name: String, type: EntityFieldType) : EntityFieldDef(name, type) {
	override fun read(entity: Any): Any? {
		if (Map::class.java.isAssignableFrom(entity.javaClass)) {
			return (entity as Map<*, *>)[key]
		} else {
			throw RuntimeException("Only map is supported, but is [$entity] now.")
		}
	}

	override fun write(entity: Any, value: Any?) {
		if (Map::class.java.isAssignableFrom(entity.javaClass)) {
			@Suppress("UNCHECKED_CAST")
			(entity as MutableMap<Any, Any?>)[key] = value
		} else {
			throw RuntimeException("Only map is supported, but is [$entity] now.")
		}
	}
}

private val id = MapEntityFieldDef(EntityColumns.OBJECT_ID, EntityFieldType.ID)
private val createdAt = MapEntityFieldDef(EntityColumns.CREATED_AT, EntityFieldType.CREATED_AT)
private val lastModifiedAt = MapEntityFieldDef(EntityColumns.LAST_MODIFIED_AT, EntityFieldType.LAST_MODIFIED_AT)

interface MapEntityDef : EntityDef

/**
 * entity definition for map.
 * 1. id/createdAt/lastModifiedAt will be installed
 * 2. no explicit definition for other fields
 * 3. property name(key) in map will be converted to field name
 *
 * note: don't use snake case as entity/property name, use plain text or camel case
 */
abstract class AbstractMapEntityDef(name: String) : AbstractEntityDef(name, listOf(id, createdAt, lastModifiedAt)),
	MapEntityDef {
	override fun toPersistObject(entity: Any): PersistObject {
		if (!Map::class.java.isAssignableFrom(entity.javaClass)) {
			throw RuntimeException("Only map is supported, but is [$entity] now.")
		}

		@Suppress("UNCHECKED_CAST")
		val map = (entity as Map<String, Any?>).map { (key, value) ->
			when (key) {
				id.key -> id.fieldName
				id.fieldName -> id.fieldName
				createdAt?.key -> createdAt.fieldName
				createdAt?.fieldName -> createdAt.fieldName
				lastModifiedAt?.key -> lastModifiedAt.fieldName
				lastModifiedAt?.fieldName -> lastModifiedAt.fieldName
				else -> toFieldName(key)
			} to value
		}.toMap().toMutableMap()
		this.removeEmptyId(map)
		this.handleLastModifiedAt(map)
		return map
	}

	override fun fromPersistObject(po: PersistObject): Any {
		return po.map { (key, value) ->
			when (key) {
				id.fieldName -> id.key
				id.key -> id.key
				createdAt?.key -> createdAt.key
				createdAt?.fieldName -> createdAt.key
				lastModifiedAt?.key -> lastModifiedAt.key
				lastModifiedAt?.fieldName -> lastModifiedAt.key
				else -> DynamicTopicKits.fromFieldName(key)
			} to value
		}.toMap().toMutableMap()
	}

	override fun toFieldName(propertyOrFactorName: String): String {
		return DynamicTopicKits.toFieldName(propertyOrFactorName)
	}

	override fun isMultipleTopicsSupported(): Boolean {
		return false
	}

	override fun isTopicSupported(entityOrTopicName: String): Boolean {
		return entityOrTopicName == key || entityOrTopicName == collectionName
	}

	override fun toCollectionName(): String {
		return collectionName
	}
}
