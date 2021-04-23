package com.imma.service.core.action

class InsertRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			topic.topicId to insertRow(topic, mapping)
		}.also {
			logger.process(
				"topicId" to it.first,
				"oldValue" to null,
				"newValue" to it.second,
				"insertCount" to 1,
				"updateCount" to 0
			)
		}
	}
}