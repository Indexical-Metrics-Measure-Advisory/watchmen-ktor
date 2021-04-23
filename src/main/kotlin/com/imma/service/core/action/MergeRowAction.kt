package com.imma.service.core.action

class MergeRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {

	fun run() {
		with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			val by = prepareBy()

			val findBy = build(topic, by)
			val oldOne = services.dynamicTopic { findOne(topic, findBy) }
				?: throw RuntimeException("Cannot find row from topic[${topic.name}] on filter[$findBy].")

			oldOne to mergeRow(topic, mapping, oldOne)
		}.also {
			logger.log(
				"oldValue" to it.first,
				"newValue" to it.second,
				"insertCount" to 0,
				"updateCount" to 1
			)
		}
	}
}