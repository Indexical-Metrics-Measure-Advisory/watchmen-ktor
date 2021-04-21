package com.imma.service.core.action

import com.imma.model.core.Factor
import com.imma.model.core.Topic
import com.imma.model.core.compute.Parameter
import com.imma.model.core.compute.ParameterJoint
import com.imma.model.core.compute.takeAsParameterJointOrThrow
import com.imma.model.core.compute.takeAsParameterOrThrow
import com.imma.model.core.mapping.RowMapping
import com.imma.model.core.mapping.takeAsRowMappingOrThrow
import com.imma.utils.neverOccur

abstract class AbstractTopicAction(private val context: ActionContext, private val logger: ActionLogger) {
	fun prepareVariableName(): String {
		return with(context) {
			val variableName = action["variableName"]?.toString()
			if (variableName.isNullOrBlank()) {
				throw RuntimeException("Variable name of action cannot be null or empty.")
			} else {
				variableName
			}
		}
	}

	fun prepareBy(): ParameterJoint {
		return with(context) {
			val by = action["by"]
			when {
				by == null -> throw RuntimeException("By of read action cannot be null.")
				by !is Map<*, *> -> throw RuntimeException("By of read action should be a map, but is [$by] now.")
				by.size == 0 -> throw RuntimeException("By of read action cannot be empty.")
				else -> takeAsParameterJointOrThrow(by)
			}
		}
	}

	fun prepareMapping(): RowMapping {
		return with(context) {
			val mapping = action["mapping"]
			@Suppress("UNCHECKED_CAST")
			when {
				mapping == null -> throw RuntimeException("Mapping of insert/merge action cannot be null.")
				mapping !is Collection<*> && mapping !is Array<*> -> throw RuntimeException("By of insert/merge action should be a collection or an array, but is [$mapping] now.")
				mapping is Collection<*> && mapping.size == 0 -> throw RuntimeException("Mapping of insert/merge action cannot be empty.")
				mapping is Array<*> && mapping.size == 0 -> throw RuntimeException("Mapping of insert/merge action cannot be empty.")
				mapping is Collection<*> -> takeAsRowMappingOrThrow(mapping as Collection<Map<*, *>>)
				mapping is Array<*> -> takeAsRowMappingOrThrow(mapping as Array<Map<*, *>>)
				else -> neverOccur()
			}
		}
	}

	fun prepareSource(): Parameter {
		return with(context) {
			when (val source = action["source"]) {
				null -> throw RuntimeException("Source of write action cannot be null.")
				!is Map<*, *> -> throw RuntimeException("Source of write action should be a map, but is [$source] now.")
				else -> takeAsParameterOrThrow(source)
			}
		}
	}

	fun prepareTopic(): Topic {
		return with(context) {
			val topicId = action["topicId"]?.toString()
			if (topicId.isNullOrBlank()) {
				throw RuntimeException("Topic id of action cannot be null or empty.")
			}

			val topic = topics[topicId] ?: services.topic {
				findTopicById(topicId)
			} ?: throw RuntimeException("Topic[$topicId] of action not found.")
			// put into memory
			topic.also {
				topics[topicId] = it
				// register to persist
				services.persist().registerDynamicTopic(topic)
			}
		}
	}

	fun prepareFactor(topic: Topic): Factor {
		return with(context) {
			val factorId = action["factorId"]?.toString()
			if (factorId.isNullOrBlank()) {
				throw RuntimeException("Factor id of action cannot be null or empty.")
			}

			topic.factors.find { it.factorId == factorId }
				?: throw RuntimeException("Factor[$factorId] of topic[${topic.topicId}] not found.")
		}
	}
}