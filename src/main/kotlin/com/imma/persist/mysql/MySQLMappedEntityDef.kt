package com.imma.persist.mysql

import com.imma.persist.defs.AbstractMappedEntityDef
import com.imma.persist.defs.EntityFieldDef
import com.imma.persist.defs.EntityFieldType
import com.imma.persist.defs.parseMappedEntity
import com.imma.persist.rdbms.RDBMSMappedEntityFieldDef
import java.beans.PropertyDescriptor

class MySQLMappedEntityFieldDef(name: String, type: EntityFieldType, descriptor: PropertyDescriptor) :
	RDBMSMappedEntityFieldDef(name, type, descriptor) {
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