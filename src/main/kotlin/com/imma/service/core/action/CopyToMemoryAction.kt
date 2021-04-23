package com.imma.service.core.action

import com.imma.model.core.compute.takeAsParameterOrThrow
import com.imma.service.core.parameter.ParameterWorker

class CopyToMemoryAction(private val context: ActionContext, private val logger: ActionLogger) {
    fun run() {
        val value = with(context) {
            val variableName = action["variableName"]?.toString()
            if (variableName.isNullOrBlank()) {
                throw RuntimeException("Variable name of copy to memory action cannot be null or empty.")
            }

            val source = action["source"]
                ?: throw RuntimeException("Source of copy to memory action cannot be null.")
            if (source !is Map<*, *>) {
                throw RuntimeException("Source of copy to memory action should be a map, but is [$source] now.")
            }

            val worker = ParameterWorker(pipeline, topics, currentOfTriggerData, variables)
            worker.computeParameter(takeAsParameterOrThrow(source)).also {
                variables[variableName] = it
            }
        }
        logger.log("value" to value)
    }
}