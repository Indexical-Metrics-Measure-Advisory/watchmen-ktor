package com.imma.persist.mango

import com.imma.persist.defs.AbstractMapEntityDef
import com.imma.persist.defs.PersistObject
import org.bson.Document
import org.bson.types.ObjectId

class MongoMapEntityDef(name: String) : AbstractMapEntityDef(name), MongoEntityDef {
	override fun fromPersistObject(po: PersistObject): Any {
		@Suppress("UNCHECKED_CAST")
		val entity = super.fromPersistObject(po) as MutableMap<String, Any?>
		val id = entity[this.getId().key]
		if (id is ObjectId) {
			entity[this.getId().key] = id.toString()
		}
		return entity
	}

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

fun createMongoMapEntityDef(entityName: String): MongoMapEntityDef {
	return MongoMapEntityDef(entityName)
}