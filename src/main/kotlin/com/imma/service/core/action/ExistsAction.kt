package com.imma.service.core.action

import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionBuilder

class ExistsAction(private val context: ActionContext, private val logger: ActionLogger) {
    fun run() {
        val value = with(context) {
            val variableName = action["variableName"]?.toString()
            if (variableName.isNullOrBlank()) {
                throw RuntimeException("Variable name of exists action cannot be null or empty.")
            }

            val topicId = action["topicId"]?.toString()
            if (topicId.isNullOrBlank()) {
                throw RuntimeException("Topic Id of exists action cannot be null or empty.")
            }

            val joint = when (val by = action["by"]) {
                null -> throw RuntimeException("By of exists action cannot be null or empty.")
                !is Map<*, *> -> throw RuntimeException("By of exists action should be a map, but is [$by] now.")
                else -> takeAsParameterJointOrThrow(by)
            }

            val topic = topics[topicId] ?: services.topic {
                findTopicById(topicId)
            } ?: throw RuntimeException("Source topic[$topicId] of exists action not found.")
            // put into memory
            topics[topicId] = topic

            services.dynamicTopic {
                exists(topic, ConditionBuilder(topic, pipeline, topics, sourceData, variables).build(joint))
            }.also {
                variables[variableName] = it
            }
        }
        logger.log(mutableMapOf("value" to value), RunType.process)
    }
}