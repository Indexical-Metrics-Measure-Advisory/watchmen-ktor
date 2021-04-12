package com.imma.persist.mango

import org.bson.Document

class MapEntityFieldDef(name: String, type: EntityFieldType) : EntityFieldDef(name, type) {
    override fun read(entity: Any): Any? {
        if (Map::class.java.isAssignableFrom(entity.javaClass)) {
            return (entity as Map<*, *>)[name]
        } else {
            throw RuntimeException("Only map is supported, but is [$entity] now.")
        }
    }

    override fun write(entity: Any, value: Any?) {
        if (Map::class.java.isAssignableFrom(entity.javaClass)) {
            @Suppress("UNCHECKED_CAST")
            (entity as MutableMap<Any, Any?>)[name] = value
        } else {
            throw RuntimeException("Only map is supported, but is [$entity] now.")
        }
    }
}

val id = MapEntityFieldDef("_id", EntityFieldType.ID)
val createdAt = MapEntityFieldDef("create_time", EntityFieldType.CREATED_AT)
val lastModifiedAt = MapEntityFieldDef("last_modify_time", EntityFieldType.LAST_MODIFIED_AT)

class MapEntityDef(name: String) : EntityDef(name, listOf(id, createdAt, lastModifiedAt)) {
    override fun toDocument(entity: Any): Document {
        if (!Map::class.java.isAssignableFrom(entity.javaClass)) {
            throw RuntimeException("Only map is supported, but is [$entity] now.")
        }

        @Suppress("UNCHECKED_CAST")
        val map = (entity as Map<String, Any?>).toMutableMap()
        this.removeEmptyId(map)
        this.handleLastModifiedAt(map)
        return Document(map)
    }

    /**
     * @return parameter itself
     */
    override fun toFactorName(propertyOrFactorName: String): String {
        return propertyOrFactorName
    }

    override fun isMultipleTopicsSupported(): Boolean {
        return false
    }

    override fun isTopicSupported(entityOrTopicName: String): Boolean {
        return entityOrTopicName == name
    }
}

fun createMapEntityDef(entityName: String): MapEntityDef {
    return MapEntityDef(entityName)
}