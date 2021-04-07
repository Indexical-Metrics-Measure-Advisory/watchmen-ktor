package com.imma.service.core.pipeline

import com.imma.model.core.Pipeline
import com.imma.model.core.Topic
import com.imma.service.Services
import com.imma.service.core.EngineLogger
import java.io.Closeable

class PipelineContext(val pipeline: Pipeline) : Closeable {
    val services: Services by lazy { Services() }

    /** logger use independent services */
    val logger: EngineLogger by lazy { EngineLogger(instanceId, Services()) }

    val instanceId: String by lazy { services.persist().nextSnowflakeId().toString() }
    val topics: MutableMap<String, Topic> by lazy {
        val topicId = pipeline.topicId
            ?: throw RuntimeException("Source topic of pipeline not defined.")

        val topic = services.topic {
            findTopicById(topicId)
        } ?: throw RuntimeException("Source topic of pipeline not found.")

        mutableMapOf(topicId to topic)
    }

    override fun close() {
        logger.close()
        services.close()
    }
}
