package com.imma.persist.oracle

import com.imma.persist.defs.AbstractMapEntityDef

class OracleMapEntityDef(name: String) : AbstractMapEntityDef(name), OracleEntityDef

fun createOracleMapEntityDef(entityName: String): OracleMapEntityDef {
	return OracleMapEntityDef(entityName)
}