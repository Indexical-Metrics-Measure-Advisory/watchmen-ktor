package com.imma.persist.mango

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EntityMapper {
    companion object {
        private val defs: ConcurrentMap<Class<*>, EntityDef> = ConcurrentHashMap()

        fun getDef(material: MapperMaterial): EntityDef {
            val name = material.entityName
            val clazz = material.entityClass
                ?: material.entity?.javaClass
                ?: throw RuntimeException("Cannot determine class on given material[$material].")

            val def = defs[clazz]
            return when {
                def != null -> def
                // TODO entity is map, and can find in dynamic topic defs
                // if entity is map, then entity name must exist
                Map::class.java.isAssignableFrom(clazz) -> createMapEntityDef(name!!)
                isMappedEntity(clazz) -> createMappedEntityDef(clazz, name).also { defs[clazz] = it }
                else -> throw RuntimeException("Unsupported material[$material].")
            }
        }
    }
}