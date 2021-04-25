package com.imma.persist.mysql

import com.imma.persist.defs.AbstractMapEntityDef

class MySQLMapEntityDef(name: String) : AbstractMapEntityDef(name), MySQLEntityDef

fun createMySQLMapEntityDef(entityName: String): MySQLMapEntityDef {
	return MySQLMapEntityDef(entityName)
}