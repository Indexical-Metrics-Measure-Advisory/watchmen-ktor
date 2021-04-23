package com.imma.service.core.action

class InsertRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			insertRow(topic, mapping)
		}.also {
			logger.log(
				"oldValue" to null,
				"newValue" to it,
				"insertCount" to 1,
				"updateCount" to 0
			)
		}
	}
}