package com.imma.service.core.action

import com.imma.service.core.log.RunType

class MergeRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context, logger) {
	fun run() {
		val value = with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			var by = prepareBy()
			val one = mutableMapOf<String, Any?>().apply {
				// TODO
			}
//			services.dynamicTopic { insertOne(topic, one) }
		}
		logger.log(mutableMapOf("newValue" to value), RunType.process)
	}
}