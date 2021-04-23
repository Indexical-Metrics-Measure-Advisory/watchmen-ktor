package com.imma.service.core.action

import com.imma.model.core.compute.takeAsParameterOrThrow

class CopyToMemoryAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context) {
	fun run() {
		with(context) {
			val variableName = action["variableName"]?.toString()
			if (variableName.isNullOrBlank()) {
				throw RuntimeException("Variable name of copy to memory action cannot be null or empty.")
			}

			val source = action["source"]
				?: throw RuntimeException("Source of copy to memory action cannot be null.")
			if (source !is Map<*, *>) {
				throw RuntimeException("Source of copy to memory action should be a map, but is [$source] now.")
			}

			compute(takeAsParameterOrThrow(source)).also {
				variables[variableName] = it
			}
		}.also {
			logger.process("value" to it)
		}
	}
}