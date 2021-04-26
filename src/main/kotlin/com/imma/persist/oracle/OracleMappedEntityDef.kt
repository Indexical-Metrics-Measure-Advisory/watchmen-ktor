package com.imma.persist.oracle

import com.imma.persist.defs.AbstractMappedEntityDef
import com.imma.persist.defs.EntityFieldDef
import com.imma.persist.defs.EntityFieldType
import com.imma.persist.defs.parseMappedEntity
import com.imma.persist.rdbms.RDBMSMappedEntityFieldDef
import java.beans.PropertyDescriptor

class OracleMappedEntityFieldDef(name: String, type: EntityFieldType, descriptor: PropertyDescriptor) :
	RDBMSMappedEntityFieldDef(name, type, descriptor) {
}

class OracleMappedEntityDef(name: String, entityClass: Class<*>, fields: List<EntityFieldDef>) :
	AbstractMappedEntityDef(name, entityClass, fields), OracleEntityDef

fun createOracleMappedEntityDef(entityClass: Class<*>, entityName: String?): OracleMappedEntityDef {
	return parseMappedEntity(entityClass, entityName).let { parsed ->
		OracleMappedEntityDef(
			parsed.name,
			parsed.entityClass,
			parsed.fields.map { (name, type, descriptor) ->
				OracleMappedEntityFieldDef(name, type, descriptor)
			}
		)
	}
}