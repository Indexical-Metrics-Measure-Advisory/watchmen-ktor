package com.imma.service.core.parameter

import com.imma.model.compute.Parameter
import com.imma.model.core.Pipeline
import com.imma.model.core.Topic
import com.imma.persist.core.Element
import com.imma.service.core.PipelineSourceData
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineVariables
import com.imma.service.core.createPipelineVariables

/**
 * condition builder for workout a expression
 * which means:
 * 1. topic/factor which should be kept must be given kept topic
 * 2. any topic in topic/factor parameter, if not (1), then must be source topic.
 * 3. any variable in constant parameter must be source topic or can be found from variables
 */
class ParameterBuilder(
    private val keptTopic: Topic,
    private val pipeline: Pipeline,
    private val topics: PipelineTopics,
    private val sourceData: PipelineSourceData,
    private val variables: PipelineVariables = createPipelineVariables()
) {
    fun build(parameter: Parameter): Element {
        TODO()
    }
}