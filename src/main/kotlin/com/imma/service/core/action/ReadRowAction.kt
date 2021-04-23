package com.imma.service.core.action

class ReadRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val variableName = prepareVariableName()
			val topic = prepareTopic()
			val joint = prepareBy()

			services.dynamicTopic {
				findOne(topic, build(topic, joint))
			}.also {
				variables[variableName] = it
			}
		}.also {
			logger.log("value" to it)
		}
	}
}