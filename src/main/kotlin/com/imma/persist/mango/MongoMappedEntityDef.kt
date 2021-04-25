package com.imma.persist.mango

import com.imma.persist.defs.*
import org.bson.Document
import org.bson.types.ObjectId
import java.beans.PropertyDescriptor

class MongoMappedEntityFieldDef(name: String, type: EntityFieldType, descriptor: PropertyDescriptor) :
	MappedEntityFieldDef(name, type, descriptor) {

	override fun write(entity: Any, value: Any?) {
		super.write(entity, if (value is ObjectId) value.toString() else value)
	}
}

class MongoMappedEntityDef(name: String, entityClass: Class<*>, fields: List<EntityFieldDef>) :
	AbstractMappedEntityDef(name, entityClass, fields), MongoEntityDef {
	override fun toDocument(entity: Any): Document {
		return Document(this.toPersistObject(entity))
	}

	override fun toDocument(entity: Any, generateId: () -> Any): Document {
		return Document(this.toPersistObject(entity, generateId))
	}

	override fun fromDocument(doc: Document): Any {
		return this.fromPersistObject(doc)
	}
}

fun createMongoMappedEntityDef(entityClass: Class<*>, entityName: String?): MongoMappedEntityDef {
	return parseMappedEntity(entityClass, entityName).let { parsed ->
		MongoMappedEntityDef(
			parsed.name,
			parsed.entityClass,
			parsed.fields.map { (name, type, descriptor) ->
				MongoMappedEntityFieldDef(name, type, descriptor)
			}
		)
	}
}