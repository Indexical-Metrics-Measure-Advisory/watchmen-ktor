package com.imma.persist.mysql

import com.imma.persist.defs.*
import java.beans.PropertyDescriptor

class MySQLMappedEntityFieldDef(name: String, type: EntityFieldType, descriptor: PropertyDescriptor) :
	MappedEntityFieldDef(name, type, descriptor) {
}

class MySQLMappedEntityDef(name: String, entityClass: Class<*>, fields: List<EntityFieldDef>) :
	AbstractMappedEntityDef(name, entityClass, fields), MySQLEntityDef

fun createMySQLMappedEntityDef(entityClass: Class<*>, entityName: String?): MySQLMappedEntityDef {
	return parseMappedEntity(entityClass, entityName).let { parsed ->
		MySQLMappedEntityDef(
			parsed.name,
			parsed.entityClass,
			parsed.fields.map { (name, type, descriptor) ->
				MySQLMappedEntityFieldDef(name, type, descriptor)
			}
		)
	}
}