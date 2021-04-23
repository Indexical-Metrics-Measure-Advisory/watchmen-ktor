package com.imma.service.dynamic

import com.imma.model.core.Topic
import com.imma.persist.core.Select
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

	fun <T> findOne(topic: Topic, where: Where): T? {
		return persist().findOne(where, Map::class.java, topic.name!!)
	}

	fun <T> findOne(topic: Topic, select: Select, where: Where): T? {
		return persist().findOne(select, where, Map::class.java, topic.name!!)
	}

	fun <T> list(topic: Topic, where: Where): List<T> {
		return persist().list(where, Map::class.java, topic.name!!)
	}

	fun <T> list(topic: Topic, select: Select, where: Where): List<T> {
		return persist().list(select, where, Map::class.java, topic.name!!)
	}
}