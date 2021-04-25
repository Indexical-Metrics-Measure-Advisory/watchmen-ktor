package com.imma.persist.mango

import com.imma.model.core.Topic
import com.imma.persist.defs.AbstractDynamicTopicDef
import com.imma.persist.defs.PersistObject
import org.bson.Document
import org.bson.types.ObjectId

class MongoDynamicTopicDef(topic: Topic) : AbstractDynamicTopicDef(topic), MongoEntityDef {
	override fun toPersistObject(entity: Any): PersistObject {
		val po = super.toPersistObject(entity)
		val idKey = this.toFieldName(this.getId().key)
		val value = po[idKey]
		po[idKey] = if (value is ObjectId) value.toString() else value
		return po
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

fun createMongoDynamicTopicDef(topic: Topic): MongoDynamicTopicDef {
	return MongoDynamicTopicDef(topic)
}
