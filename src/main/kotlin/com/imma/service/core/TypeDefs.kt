package com.imma.service.core

import com.imma.model.core.Topic

typealias PipelineTopics = MutableMap<String, Topic>
typealias PipelineSourceData = Map<String, Any?>
typealias PipelineVariables = MutableMap<String, Any?>

fun createPipelineTopics(vararg topics: Pair<String, Topic>): PipelineTopics {
    return mutableMapOf(*topics)
}

fun createPipelineVariables(): PipelineVariables {
    return mutableMapOf()
}
