package com.imma.service.core.action

import com.imma.persist.core.select

class ReadFactorAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val variableName = prepareVariableName()
			val topic = prepareTopic()
			val factor = prepareFactor(topic)
			val joint = prepareBy()
			services.dynamicTopic {
				findOne(topic, select { factor(factor.name!!) }, build(topic, joint))
			}.also {
				variables[variableName] = it
			}
		}.also {
			logger.log("value" to it)
		}
	}
}