package com.imma.persist.mango

import com.imma.persist.annotation.*
import org.bson.Document
import java.beans.PropertyDescriptor

class MappedEntityFieldDef(name: String, type: EntityFieldType, private val descriptor: PropertyDescriptor) :
    EntityFieldDef(name, type) {

    override fun read(entity: Any): Any? {
        return descriptor.readMethod.invoke(entity)
    }

    override fun write(entity: Any, value: Any?) {
        descriptor.writeMethod.invoke(entity, value)
    }
}

class MappedEntityDef(name: String, fields: List<EntityFieldDef>) : EntityDef(name, fields) {
    override fun toDocument(entity: Any): Document {
        val map = fields.map { field ->
            val name = field.name
            val value = field.read(entity)
            name to value
        }.toMap().toMutableMap()

        this.removeEmptyId(map)
        this.handleLastModifiedAt(map)
        return Document(map)
    }

    override fun toFactorName(propertyOrFactorName: String): String {
        return fields.find { it.name == propertyOrFactorName }?.name ?: propertyOrFactorName
    }

    override fun isMultipleTopicsSupported(): Boolean {
        return false
    }

    override fun isTopicSupported(entityOrTopicName: String): Boolean {
        return entityOrTopicName == name
    }
}

fun isMappedEntity(entityClass: Class<*>): Boolean {
    return entityClass.isAnnotationPresent(Entity::class.java)
}

fun createMappedEntityDef(entityClass: Class<*>, entityName: String?): MappedEntityDef {
    val entity = entityClass.getAnnotation(Entity::class.java)
        ?: throw RuntimeException("Class[$entityClass] is not an entity.")

    var name = entityName
    if (name.isNullOrBlank()) {
        name = entity.value
    }
    if (name.isNullOrBlank()) {
        throw RuntimeException("Entity name not defined for class[$entityClass].")
    }

    val fields = entityClass.declaredFields.map { field ->
        when {
            field.isAnnotationPresent(Id::class.java) -> {
                @Suppress("NAME_SHADOWING") val name = field.getAnnotation(Id::class.java).value
                MappedEntityFieldDef(name, EntityFieldType.ID, PropertyDescriptor(field.name, field.declaringClass))
            }
            field.isAnnotationPresent(CreatedAt::class.java) -> {
                @Suppress("NAME_SHADOWING") val name = field.getAnnotation(CreatedAt::class.java).value
                MappedEntityFieldDef(
                    name,
                    EntityFieldType.CREATED_AT,
                    PropertyDescriptor(field.name, field.declaringClass)
                )
            }
            field.isAnnotationPresent(LastModifiedAt::class.java) -> {
                @Suppress("NAME_SHADOWING") val name = field.getAnnotation(LastModifiedAt::class.java).value
                MappedEntityFieldDef(
                    name,
                    EntityFieldType.LAST_MODIFIED_AT,
                    PropertyDescriptor(field.name, field.declaringClass)
                )
            }
            field.isAnnotationPresent(Field::class.java) -> {
                @Suppress("NAME_SHADOWING") val name = field.getAnnotation(Field::class.java).value
                MappedEntityFieldDef(
                    name,
                    EntityFieldType.REGULAR,
                    PropertyDescriptor(field.name, field.declaringClass)
                )
            }
            else -> null
        }
    }.filterNotNull()

    return MappedEntityDef(name, fields)
}