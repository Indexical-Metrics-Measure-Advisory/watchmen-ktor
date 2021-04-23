package com.imma.service.core.action

class WriteFactorAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		val value = with(context) {
			val topic = prepareTopic()
			val factor = prepareFactor(topic)
			val source = prepareSource()
			var by = prepareBy()
			val one = mutableMapOf<String, Any?>().apply {
				// TODO
			}
//			services.dynamicTopic { insertOne(topic, one) }
		}.also {
			logger.log("newValue" to it)
		}
	}
}