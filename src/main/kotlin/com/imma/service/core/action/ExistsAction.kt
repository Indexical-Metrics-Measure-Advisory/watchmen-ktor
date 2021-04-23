package com.imma.service.core.action

import com.imma.service.core.parameter.ConditionBuilder

class ExistsAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context, logger) {
	fun run() {
		val value = with(context) {
			val variableName = prepareVariableName()
			val topic = prepareTopic()
			val joint = prepareBy()
			services.dynamicTopic {
				exists(topic, ConditionBuilder(topic, pipeline, topics, currentOfTriggerData, variables).build(joint))
			}.also {
				variables[variableName] = it
			}
		}
		logger.log("value" to value)
	}
}