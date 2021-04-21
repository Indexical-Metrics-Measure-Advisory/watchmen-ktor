package com.imma.service.core.action

import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionBuilder

class InsertRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context, logger) {
	fun run() {
		val value = with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			services.dynamicTopic {
//				exists(topic, ConditionBuilder(topic, pipeline, topics, sourceData, variables).build(joint))
			}
		}
		logger.log(mutableMapOf("value" to value), RunType.process)
	}
}