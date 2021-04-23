package com.imma.service.core.action

import com.imma.persist.core.select
import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionBuilder

class ReadFactorAction(private val context: ActionContext, private val logger: ActionLogger) :
    AbstractTopicAction(context, logger) {
    fun run() {
        val value = with(context) {
            val variableName = prepareVariableName()
            val topic = prepareTopic()
            val factor = prepareFactor(topic)
            val joint = prepareBy()
            val row: Any? = services.dynamicTopic {
                findOne(topic, select {
                    factor(factor.name!!)
                }, ConditionBuilder(topic, pipeline, topics, sourceData, variables).build(joint))
            }
            variables[variableName] = row
            row
        }
        logger.log("value" to value)
    }
}