package com.imma.service.core.action

import com.imma.model.compute.takeAsParameterOrThrow
import com.imma.service.core.parameter.ParameterWorker

class CopyToMemoryAction(private val context: ActionContext, private val logger: ActionLogger) {
    fun run() {
        with(context) {
            val variableName = action["variableName"]?.toString()
            if (variableName.isNullOrEmpty()) {
                throw RuntimeException("Variable name of action[${action.actionId}] cannot be null or empty.")
            }

            val source = action["source"]
                ?: throw RuntimeException("Source of action[${action.actionId}] cannot be null.")
            if (source !is Map<*, *>) {
                throw RuntimeException("Source of action[${action.actionId}] should be a map, but is [$source] now.")
            }

            val worker = ParameterWorker(topics, sourceData, variables)
            variables[variableName] = worker.computeParameter(takeAsParameterOrThrow(source))
        }
    }
}