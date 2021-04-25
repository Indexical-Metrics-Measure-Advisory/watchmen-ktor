package com.imma.persist.mango

import com.imma.persist.defs.EntityDef
import org.bson.Document

interface MongoEntityDef : EntityDef {
	/**
	 * convert given entity to document
	 */
	fun toDocument(entity: Any): Document

	fun toDocument(entity: Any, generateId: () -> Any): Document

	/**
	 * convert given document to entity
	 */
	fun fromDocument(doc: Document): Any
}
