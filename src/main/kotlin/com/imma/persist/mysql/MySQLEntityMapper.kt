package com.imma.persist.mysql

import com.imma.model.core.Topic
import com.imma.persist.defs.DynamicTopicDef
import com.imma.persist.defs.EntityMapper

class MySQLEntityMapper {
	companion object {
		private val mapper = EntityMapper(
			{ topic -> createMySQLDynamicTopicDef(topic) },
			{ name -> createMySQLMapEntityDef(name) },
			{ clazz, name -> createMySQLMappedEntityDef(clazz, name) }
		)

		/**
		 * return old definition when exists
		 */
		fun registerDynamicTopic(topic: Topic): DynamicTopicDef {
			return mapper.registerDynamicTopic(topic)
		}

		fun getDef(material: MySQLMapperMaterial): MySQLEntityDef {
			return mapper.getDef(material) as MySQLEntityDef
		}
	}
}