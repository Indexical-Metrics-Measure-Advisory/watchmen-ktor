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

			Triple(topic.topicId, oldOne, mergeRow(topic, mapping, oldOne))
		}.also {
			logger.process(
				"topicId" to it.first,
				"oldValue" to it.second,
				"newValue" to it.third,
				"insertCount" to 0,
				"updateCount" to 1
			)
		}
	}
}