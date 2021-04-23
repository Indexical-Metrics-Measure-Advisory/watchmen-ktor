package com.imma.service.core.action

import com.imma.service.core.parameter.ConditionBuilder

class ReadRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context, logger) {
	fun run() {
		val value = with(context) {
			val variableName = prepareVariableName()
			val topic = prepareTopic()
			val joint = prepareBy()
			val row: Any? = services.dynamicTopic {
				findOne(topic, ConditionBuilder(topic, pipeline, topics, sourceData, variables).build(joint))
			}
			variables[variableName] = row
			row
		}
		logger.log("value" to value)
	}
}