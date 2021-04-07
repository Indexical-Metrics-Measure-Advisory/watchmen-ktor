package com.imma.service.core.action

import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.service.core.parameter.ConditionWorker

class AlarmAction(private val context: ActionContext, private val logger: ActionLogger) {
    private fun shouldRun(): Boolean {
        return context.run {
            val conditional = action["conditional"]
            val on = action["on"]

            when {
                conditional == false -> true
                !conditional.toString().toBoolean() -> true
                on == null -> true
                on !is Map<*, *> -> throw RuntimeException("Unsupported condition found in alarm action.")
                else -> ConditionWorker(topics, sourceData, variables).computeJoint(takeAsParameterJointOrThrow(on))
            }
        }
    }

    fun run() {
        if (shouldRun()) {
            // TODO
        } else {
            logger.ignore("Alarm action ignored because of condition not reached.")
        }
    }
}