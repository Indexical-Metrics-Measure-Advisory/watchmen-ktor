package com.imma.persist.rdbms

import com.imma.persist.defs.EntityFieldType
import com.imma.persist.defs.MappedEntityFieldDef
import java.beans.PropertyDescriptor

/**
 * for RDBMS, mapped entity field with List type will be convert between List and JSON Array.
 */
open class RDBMSMappedEntityFieldDef(name: String, type: EntityFieldType, private val descriptor: PropertyDescriptor) :
	MappedEntityFieldDef(name, type, descriptor) {

	override fun read(entity: Any): Any? {
		val reader = descriptor.readMethod
		val value = descriptor.readMethod.invoke(entity) ?: return null

		return if (reader.returnType == List::class.java) {
			// flatten to json array
			(value as List<*>).joinToString(separator = ",", prefix = "[", postfix = "]") {
				if (it == null) "null" else "\"$it\""
			}
		} else {
			value
		}
	}

	override fun write(entity: Any, value: Any?) {
		val writer = descriptor.writeMethod
		if (value == null) {
			writer.invoke(entity, null)
		} else if (writer.parameterTypes[0] == List::class.java) {
			val list = value.toString()
				.replace("[", "")
				.replace("]", "")
				.split(",")
				.map { it.trim { char -> char == '"' } }
			writer.invoke(entity, list)
		} else {
			writer.invoke(entity, null)
		}
	}
}
