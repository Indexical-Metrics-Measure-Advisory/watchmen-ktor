package com.imma.service.core.action

import com.imma.model.core.mapping.WriteAggregateArithmetic
import com.imma.service.core.parameter.ParameterBuilder

class InsertRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context, logger) {
	fun run() {
		val value = with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			val one = mapping.map { row ->
				val (source, factorId, arithmetic) = row

				if (arithmetic == WriteAggregateArithmetic.count) {
					// the first one, count always be 1
					factorId to 1
				} else {
					// the first one, arithmetic is ignored
					factorId to ParameterBuilder(topic, pipeline, topics, sourceData, variables).buildParameter(source)
				}
			}.toMap().toMutableMap()
			services.dynamicTopic { insertOne(topic, one) }
		}
		logger.log("oldValue" to null, "newValue" to value, "insertCount" to 1, "updateCount" to 0)
	}
}