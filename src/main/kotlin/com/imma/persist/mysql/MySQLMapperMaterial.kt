package com.imma.persist.mysql

import com.imma.persist.defs.EntityDef
import com.imma.persist.rdbms.RDBMSMapperMaterial

class MySQLMapperMaterial(
	entity: Any?,
	entityClass: Class<*>? = null,
	entityName: String? = null
) : RDBMSMapperMaterial(entity, entityClass, entityName) {
	private val def: MySQLEntityDef = MySQLEntityMapper.getDef(this)

	override fun getDef(): EntityDef {
		return this.def
	}
}

class MySQLMapperMaterialBuilder private constructor(private val entity: Any?) {
	private var clazz: Class<*>? = null
	private var name: String? = null

	companion object {
		fun create(entity: Any? = null): MySQLMapperMaterialBuilder {
			return MySQLMapperMaterialBuilder(entity)
		}
	}

	fun type(clazz: Class<*>): MySQLMapperMaterialBuilder {
		this.clazz = clazz
		return this
	}

	fun name(name: String): MySQLMapperMaterialBuilder {
		this.name = name
		return this
	}

	fun build(): MySQLMapperMaterial {
		return MySQLMapperMaterial(entity, clazz, name)
	}
}