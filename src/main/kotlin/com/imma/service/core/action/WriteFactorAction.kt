package com.imma.service.core.action

import com.imma.model.core.mapping.RowMapping
import com.imma.model.core.mapping.takeAsFactorMappingOrThrow

class WriteFactorAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val topic = prepareTopic()
			val factor = prepareFactor(topic)
			val source = prepareSource()
			val arithmetic = prepareArithmetic()
			val by = prepareBy()

			val findBy = build(topic, by)
			val oldOne = services.dynamicTopic { findOne(topic, findBy) }
				?: throw RuntimeException("Cannot find row from topic[${topic.name}] on filter[$findBy].")

			val mapping: RowMapping = mapOf(
				"source" to source,
				"factorId" to factor.factorId,
				"arithmetic" to arithmetic
			).let {
				takeAsFactorMappingOrThrow(it)
			}.let {
				mutableListOf(it)
			}

			// still raise whole data to trigger next pipeline, if exists
			Triple(topic.topicId, oldOne, mergeRow(topic, mapping, oldOne))
		}.also {
			logger.process(
				"topicId" to it.first,
				"oldValue" to it.second,
				"newValue" to it.third,
				"insertCount" to 0,
				"updateCount" to 1,
			)
		}
	}
}