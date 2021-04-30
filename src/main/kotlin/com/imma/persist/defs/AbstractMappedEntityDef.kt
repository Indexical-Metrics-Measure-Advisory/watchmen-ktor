package com.imma.persist.defs

import com.imma.persist.DynamicTopicKits
import com.imma.persist.annotation.*
import java.beans.PropertyDescriptor

open class MappedEntityFieldDef(name: String, type: EntityFieldType, private val descriptor: PropertyDescriptor) :
	EntityFieldDef(name, type) {

	fun getPropertyName(): String {
		return descriptor.name
	}

	override fun read(entity: Any): Any? {
		return descriptor.readMethod.invoke(entity)
	}

	override fun write(entity: Any, value: Any?) {
		descriptor.writeMethod.invoke(entity, value)
	}
}

interface MappedEntityDef : EntityDef

abstract class AbstractMappedEntityDef(
	name: String,
	protected val entityClass: Class<*>,
	fields: List<EntityFieldDef>
) :
	AbstractEntityDef(name, fields), MappedEntityDef {
	override fun toPersistObject(entity: Any): PersistObject {
		val map = fields.associate { field ->
			field.fieldName to field.read(entity)
		}.toMutableMap()

		this.removeEmptyId(map)
		this.handleLastModifiedAt(map)
		return map
	}

	override fun fromPersistObject(po: PersistObject): Any {
		return entityClass.getConstructor().newInstance().also {
			fields.forEach { field ->
				field.write(this, po[field.fieldName])
			}
		}
	}

	override fun toFieldName(propertyOrFactorName: String): String {
		val field = fields.find {
			val field = it as MappedEntityFieldDef
			field.getPropertyName() == propertyOrFactorName
					|| field.key == propertyOrFactorName
					|| field.fieldName.equals(propertyOrFactorName, true)
		}
		return field?.fieldName ?: DynamicTopicKits.toFieldName(propertyOrFactorName)
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

data class MappedEntityField(
	val name: String,
	val type: EntityFieldType,
	val descriptor: PropertyDescriptor
)

data class MappedEntity(
	val name: String,
	val entityClass: Class<*>,
	val fields: List<MappedEntityField>
)

fun parseMappedEntity(entityClass: Class<*>, entityName: String?): MappedEntity {
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
				MappedEntityField(name, EntityFieldType.ID, PropertyDescriptor(field.name, field.declaringClass))
			}
			field.isAnnotationPresent(CreatedAt::class.java) -> {
				@Suppress("NAME_SHADOWING") val name = field.getAnnotation(CreatedAt::class.java).value
				MappedEntityField(
					name,
					EntityFieldType.CREATED_AT,
					PropertyDescriptor(field.name, field.declaringClass)
				)
			}
			field.isAnnotationPresent(LastModifiedAt::class.java) -> {
				@Suppress("NAME_SHADOWING") val name = field.getAnnotation(LastModifiedAt::class.java).value
				MappedEntityField(
					name,
					EntityFieldType.LAST_MODIFIED_AT,
					PropertyDescriptor(field.name, field.declaringClass)
				)
			}
			field.isAnnotationPresent(Field::class.java) -> {
				@Suppress("NAME_SHADOWING") val name = field.getAnnotation(Field::class.java).value
				MappedEntityField(
					name,
					EntityFieldType.REGULAR,
					PropertyDescriptor(field.name, field.declaringClass)
				)
			}
			else -> null
		}
	}

	return MappedEntity(name, entityClass, fields)
}