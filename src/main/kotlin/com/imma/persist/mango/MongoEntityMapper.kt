package com.imma.persist.mango

import com.imma.model.core.Topic
import com.imma.persist.defs.DynamicTopicDef
import com.imma.persist.defs.EntityMapper

class MongoEntityMapper {
	companion object {
		private val mapper = EntityMapper(
			{ topic -> createMongoDynamicTopicDef(topic) },
			{ name -> createMongoMapEntityDef(name) },
			{ clazz, name -> createMongoMappedEntityDef(clazz, name) }
		)

		/**
		 * return old definition when exists
		 */
		fun registerDynamicTopic(topic: Topic): DynamicTopicDef {
			return mapper.registerDynamicTopic(topic)
		}

		fun getDef(material: MongoMapperMaterial): MongoEntityDef {
			return mapper.getDef(material) as MongoEntityDef
		}
	}
}