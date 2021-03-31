package com.imma.service.core

import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.model.core.Pipeline
import com.imma.model.core.Topic
import com.imma.service.Services
import java.io.Closeable

class PipelineWorker(private val pipeline: Pipeline) : Closeable {
    private val services: Services by lazy { Services() }
    private val instanceId: String by lazy { services.persist().nextSnowflakeId().toString() }

    // logger use independent services
    private val logger: LoggerWorker by lazy { LoggerWorker(instanceId, Services()) }

    private fun findPipelineSourceTopic(pipeline: Pipeline): MutableMap<String, Topic> {
        val topicId =
            pipeline.topicId ?: throw RuntimeException("Source topic of pipeline not defined.")

        val topic = services.topic {
            findTopicById(topicId)
        } ?: throw RuntimeException("Source topic of pipeline not found.")

        return mutableMapOf(topicId to topic)
    }

    private fun shouldRun(
        pipeline: Pipeline,
        topics: MutableMap<String, Topic>,
        sourceData: Map<String, Any>
    ): Boolean {
        if (!pipeline.conditional || pipeline.on.isNullOrEmpty()) {
            return true
        }

        val joint = pipeline.on.takeAsParameterJointOrThrow()

        return ConditionWorker(topics, sourceData, mutableMapOf()).computeJoint(joint)
    }

    private fun doRun(pipeline: Pipeline, data: TriggerData) {
        logger.log("Start to run pipeline.", PipelineRunType.start)

        try {
            val topics: MutableMap<String, Topic> = findPipelineSourceTopic(pipeline)
            pipeline.takeIf { shouldRun(it, topics, data.now) }?.let {
                // TODO run pipeline body
            }
        } catch (t: Throwable) {
            logger.error("Failed to run pipeline.", t)
        } finally {
            logger.log("End of run pipeline.", PipelineRunType.end)
        }
    }


    fun run(data: TriggerData) {
        when {
            !pipeline.validated -> logger.log("Pipeline is invalidated.", PipelineRunType.invalidate)
            !pipeline.enabled -> logger.log("Pipeline is not enabled.", PipelineRunType.disable)
            else -> doRun(pipeline, data)
        }
    }

    override fun close() {
        services.close()
    }
}