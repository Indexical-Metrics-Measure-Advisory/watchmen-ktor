package com.imma.service.core.parameter

import com.imma.model.compute.Parameter
import com.imma.model.compute.ParameterKind
import com.imma.model.core.Pipeline
import com.imma.model.core.Topic
import com.imma.service.core.RunContext

/**
 * parameter worker for workout a constant value.
 * which means:
 * 1. any topic in topic/factor parameter must be source topic.
 * 2. any variable in constant parameter must be source topic or can be found from variables
 */
class ParameterWorker(
    private val pipeline: Pipeline,
    private val topics: MutableMap<String, Topic>,
    private val sourceData: Map<String, Any>,
    private val variables: MutableMap<String, Any?> = mutableMapOf()
) : RunContext {
    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }

    fun computeParameter(param: Parameter): Any? {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (param.kind) {
            ParameterKind.topic -> {
                // TODO
            }
            ParameterKind.constant -> {
                // TODO
            }
            ParameterKind.computed -> {
                // TODO
            }
            else -> throw RuntimeException("Unsupported parameter[$param].")
        }
    }
}