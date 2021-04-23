package com.imma.service.core.action

class ExistsAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val variableName = prepareVariableName()
			val topic = prepareTopic()
			val joint = prepareBy()
			services.dynamicTopic {
				exists(topic, build(topic, joint))
			}.also {
				variables[variableName] = it
			}
		}.also {
			logger.process("value" to it)
		}
	}
}