package com.imma.service.core

import com.imma.model.compute.Parameter
import com.imma.model.compute.ParameterKind
import com.imma.model.core.Topic

class ParameterWorker(
    private val topics: MutableMap<String, Topic>,
    private val sourceData: Map<String, Any>,
    private val variables: MutableMap<String, Any> = mutableMapOf()
) {
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