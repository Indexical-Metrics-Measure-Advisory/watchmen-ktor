package com.imma.service.core.pipeline

import com.imma.model.core.Pipeline
import com.imma.service.Services
import com.imma.service.core.EngineLogger
import com.imma.service.core.PipelineTopics
import com.imma.service.core.RunContext
import com.imma.service.core.createPipelineTopics
import java.io.Closeable

class PipelineContext(val pipeline: Pipeline) : RunContext, Closeable {
    val services: Services by lazy { Services() }

    /** logger use independent services */
    val logger: EngineLogger by lazy { EngineLogger(instanceId, Services()) }

    val instanceId: String by lazy { services.persist().nextSnowflakeId().toString() }
    val topics: PipelineTopics by lazy {
        val topicId = pipeline.topicId
            ?: throw RuntimeException("Source topic of pipeline not defined.")

        val topic = services.topic {
            findTopicById(topicId)
        } ?: throw RuntimeException("Source topic of pipeline not found.")

        createPipelineTopics(topicId to topic)
    }

    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }

    override fun close() {
        logger.close()
        services.close()
    }
}
