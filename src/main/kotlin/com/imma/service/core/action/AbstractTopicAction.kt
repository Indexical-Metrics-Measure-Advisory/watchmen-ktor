package com.imma.service.core.action

import com.imma.model.compute.ParameterJoint
import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.model.core.Factor
import com.imma.model.core.Topic

abstract class AbstractTopicAction(private val context: ActionContext, private val logger: ActionLogger) {
    fun prepareVariableName(): String {
        return with(context) {
            val variableName = action["variableName"]?.toString()
            if (variableName.isNullOrBlank()) {
                throw RuntimeException("Variable name of action cannot be null or empty.")
            } else {
                variableName
            }
        }
    }

    fun prepareBy(): ParameterJoint {
        return with(context) {
            when (val by = action["by"]) {
                null -> throw RuntimeException("By of read action cannot be null or empty.")
                !is Map<*, *> -> throw RuntimeException("By of read action should be a map, but is [$by] now.")
                else -> takeAsParameterJointOrThrow(by)
            }
        }
    }

    fun prepareTopic(): Topic {
        return with(context) {
            val topicId = action["topicId"]?.toString()
            if (topicId.isNullOrBlank()) {
                throw RuntimeException("Topic id of action cannot be null or empty.")
            }


            val topic = topics[topicId] ?: services.topic {
                findTopicById(topicId)
            } ?: throw RuntimeException("Topic[$topicId] of action not found.")
            // put into memory
            topic.also {
                topics[topicId] = it
                // register to persist
                services.persist().registerDynamicTopic(topic)
            }
        }
    }

    fun prepareFactor(topic: Topic): Factor {
        return with(context) {
            val factorId = action["factorId"]?.toString()
            if (factorId.isNullOrBlank()) {
                throw RuntimeException("Factor id of action cannot be null or empty.")
            }

            topic.factors.find { it.factorId == factorId }
                ?: throw RuntimeException("Factor[$factorId] of topic[${topic.topicId}] not found.")
        }
    }
}