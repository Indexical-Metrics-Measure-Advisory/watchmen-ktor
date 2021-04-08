package com.imma.service.core.action

import com.imma.model.compute.ParameterJoint
import com.imma.model.compute.ParameterJointType
import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.persist.core.And
import com.imma.persist.core.Or
import com.imma.persist.core.Where
import com.imma.service.core.log.RunType

private fun Or.build(joint: ParameterJoint): Or {
    return this
}

private fun And.build(joint: ParameterJoint): And {
    return this
}

private fun ParameterJoint.build(): Where {
    return if (jointType === ParameterJointType.and)
        And().build(this)
    else
        Or().build(this)
}

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

            val value = services.dynamicTopic { exists(topic, joint.build()) }
            variables[variableName] = value
            value
        }
        logger.log(mutableMapOf("value" to value), RunType.process)
    }
}