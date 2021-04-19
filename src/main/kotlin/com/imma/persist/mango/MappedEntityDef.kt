package com.imma.persist.mango

import com.imma.persist.annotation.*
import org.bson.Document
import org.bson.types.ObjectId
import java.beans.PropertyDescriptor

class MappedEntityFieldDef(name: String, type: EntityFieldType, private val descriptor: PropertyDescriptor) :
    EntityFieldDef(name, type) {

    fun getPropertyName(): String {
        return descriptor.name
    }

    override fun read(entity: Any): Any? {
        return descriptor.readMethod.invoke(entity)
    }

    override fun write(entity: Any, value: Any?) {
        val v = if (value is ObjectId) value.toString() else value
        descriptor.writeMethod.invoke(entity, v)
    }
}

class MappedEntityDef(name: String, private val entityClass: Class<*>, fields: List<EntityFieldDef>) :
    EntityDef(name, fields) {
    override fun toDocument(entity: Any): Document {
        val map = fields.map { it ->
            val field = it as MappedEntityFieldDef
            val name = field.fieldName
            val value = field.read(entity)
            name to value
        }.toMap().toMutableMap()

        this.removeEmptyId(map)
        this.handleLastModifiedAt(map)
        return Document(map)
    }

    private fun createEntity(): Any {
        val constructor = entityClass.getConstructor()
        return constructor.newInstance()
    }

    override fun fromDocument(doc: Document): Any {
        return this.createEntity().also {
            fields.forEach {
                val field = it as MappedEntityFieldDef
                field.write(this, doc[field.fieldName])
            }
        }
    }

    override fun toFieldName(propertyOrFactorName: String): String {
        val field = fields.find {
            val field = it as MappedEntityFieldDef
            field.getPropertyName() == propertyOrFactorName
                    || field.key == propertyOrFactorName
                    || field.fieldName == propertyOrFactorName
        }
        return if (field == null) {
            propertyOrFactorName
        } else {
            (field as MappedEntityFieldDef).fieldName
        }
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

    val fields = entityClass.declaredFields.mapNotNull { field ->
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
    }

    return MappedEntityDef(name, entityClass, fields)
}