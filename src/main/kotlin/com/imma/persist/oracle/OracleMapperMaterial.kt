package com.imma.persist.oracle

import com.imma.persist.defs.EntityDef
import com.imma.persist.rdbms.RDBMSMapperMaterial

class OracleMapperMaterial(
	entity: Any?,
	entityClass: Class<*>? = null,
	entityName: String? = null
) : RDBMSMapperMaterial(entity, entityClass, entityName, OracleFunctions()) {
	private val def: OracleEntityDef = OracleEntityMapper.getDef(this)

	override fun getDef(): EntityDef {
		return this.def
	}
}

class OracleMapperMaterialBuilder private constructor(private val entity: Any?) {
	private var clazz: Class<*>? = null
	private var name: String? = null

	companion object {
		fun create(entity: Any? = null): OracleMapperMaterialBuilder {
			return OracleMapperMaterialBuilder(entity)
		}
	}

	fun type(clazz: Class<*>): OracleMapperMaterialBuilder {
		this.clazz = clazz
		return this
	}

	fun name(name: String): OracleMapperMaterialBuilder {
		this.name = name
		return this
	}

	fun build(): OracleMapperMaterial {
		return OracleMapperMaterial(entity, clazz, name)
	}
}