package com.imma.persist.defs

import com.imma.model.core.Topic
import com.imma.utils.nothing
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EntityMapper(
	private val createDynamicTopicDef: (topic: Topic) -> DynamicTopicDef,
	private val createMapEntityDef: (name: String) -> MapEntityDef,
	private val createMappedEntityDef: (entityClass: Class<*>, name: String?) -> MappedEntityDef
) {
	private val dynamicTopicDefs: ConcurrentMap<String, DynamicTopicDef> = ConcurrentHashMap()
	private val defs: ConcurrentMap<Class<*>, EntityDef> = ConcurrentHashMap()

	private fun isDynamicTopic(maybeTopicId: String?): Boolean {
		return if (maybeTopicId.isNullOrBlank()) false else dynamicTopicDefs.contains(maybeTopicId)
	}

	private fun getDynamicTopic(topicId: String): DynamicTopicDef {
		return dynamicTopicDefs[topicId] ?: throw RuntimeException("Topic[id=$topicId] not found.")
	}

	/**
	 * return old definition when exists
	 */
	fun registerDynamicTopic(topic: Topic): DynamicTopicDef {
		val topicId = topic.topicId
		if (topicId.isNullOrBlank()) {
			throw RuntimeException("Topic[$topic] with null or blank id cannot be registered.")
		}
		var existsDef = dynamicTopicDefs[topicId]
		val replace = { createDynamicTopicDef(topic).also { dynamicTopicDefs[topicId] } }
		when {
			existsDef == null -> existsDef = replace()
			// existing is unknown, replace existing
			existsDef.getTopic().lastModifyTime == null -> existsDef = replace()
			// given is unknown, replace existing
			topic.lastModifyTime == null -> existsDef = replace()
			// existing is before given, replace existing
			existsDef.getTopic().lastModifyTime!!.before(topic.lastModifyTime) -> existsDef = replace()
			// existing is after given, keep it
			else -> nothing()
		}

		return existsDef
	}

	fun getDef(material: MapperMaterial): EntityDef {
		val name = material.entityName
		val clazz = material.entityClass
			?: material.entity?.javaClass
			?: throw RuntimeException("Cannot determine class on given material[$material].")

		val def = defs[clazz]
		return when {
			def != null -> def
			// entity is map, and can find in dynamic topic defs
			!name.isNullOrBlank() && isDynamicTopic(name) -> getDynamicTopic(name)
			// if entity is map, then entity name must exist
			!name.isNullOrBlank() && Map::class.java.isAssignableFrom(clazz) -> createMapEntityDef(name)
			isMappedEntity(clazz) -> createMappedEntityDef(clazz, name).also { defs[clazz] = it }
			else -> throw RuntimeException("Unsupported material[$material].")
		}
	}
}