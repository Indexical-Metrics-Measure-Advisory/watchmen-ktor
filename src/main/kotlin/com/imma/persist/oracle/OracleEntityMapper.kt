package com.imma.persist.oracle

import com.imma.model.core.Topic
import com.imma.persist.defs.DynamicTopicDef
import com.imma.persist.defs.EntityMapper

class OracleEntityMapper {
	companion object {
		private val mapper = EntityMapper(
			{ topic -> createOracleDynamicTopicDef(topic) },
			{ name -> createOracleMapEntityDef(name) },
			{ clazz, name -> createOracleMappedEntityDef(clazz, name) }
		)

		/**
		 * return old definition when exists
		 */
		fun registerDynamicTopic(topic: Topic): DynamicTopicDef {
			return mapper.registerDynamicTopic(topic)
		}

		fun getDef(material: OracleMapperMaterial): OracleEntityDef {
			return mapper.getDef(material) as OracleEntityDef
		}
	}
}