package com.imma.service.core.action

class InsertOrMergeRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			val by = prepareBy()

			val findBy = build(topic, by)
			val oldOne = services.dynamicTopic { findOne(topic, findBy) }

			if (oldOne == null) {
				Triple(topic.topicId, oldOne, insertRow(topic, mapping))
			} else {
				Triple(topic.topicId, oldOne, mergeRow(topic, mapping, oldOne))
			}
		}.also {
			logger.log(
				"topicId" to it.first,
				"oldValue" to it.second,
				"newValue" to it.third,
				// old value not exists, insert 1; otherwise insert 0
				"insertCount" to if (it.second == null) 1 else 0,
				// old value not exists, update 0; otherwise update 1
				"updateCount" to if (it.second == null) 0 else 1
			)
		}
	}
}