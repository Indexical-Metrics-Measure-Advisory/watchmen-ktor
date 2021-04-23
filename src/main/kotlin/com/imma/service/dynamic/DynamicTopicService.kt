package com.imma.service.dynamic

import com.imma.model.core.Topic
import com.imma.persist.core.Select
import com.imma.persist.core.Updates
import com.imma.persist.core.Where
import com.imma.service.Service
import com.imma.service.Services

class DynamicTopicService(services: Services) : Service(services) {
	fun insertOne(topic: Topic, one: Map<String, *>) {
		persist().insertOne(one, Map::class.java, topic.name!!)
	}

	fun exists(topic: Topic, where: Where): Boolean {
		return persist().exists(where, Map::class.java, topic.name!!)
	}

	fun findOne(topic: Topic, where: Where): Map<String, *>? {
		return persist().findOne(where, Map::class.java, topic.name!!)
	}

	fun findOne(topic: Topic, select: Select, where: Where): Map<String, *>? {
		return persist().findOne(select, where, Map::class.java, topic.name!!)
	}

	fun updateOne(topic: Topic, updates: Updates, where: Where) {
		persist().updateOne(where, updates, Map::class.java, topic.name!!)
	}

	fun list(topic: Topic, where: Where): List<Map<String, *>> {
		return persist().list(where, Map::class.java, topic.name!!)
	}

	fun list(topic: Topic, select: Select, where: Where): List<Map<String, *>> {
		return persist().list(select, where, Map::class.java, topic.name!!)
	}
}