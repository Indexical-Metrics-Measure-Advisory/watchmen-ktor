package com.imma.service.core

import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.model.core.Pipeline
import com.imma.model.core.Topic
import com.imma.service.Services
import java.io.Closeable

class PipelineWorker(private val pipeline: Pipeline) : Closeable {
    private val services: Services by lazy { Services() }

    // logger use independent services
    private val logger: LoggerWorker by lazy { LoggerWorker(Services()) }
    private val instanceId: String by lazy { services.persist().nextSnowflakeId().toString() }

    private fun log(msg: String, runType: PipelineRunType) {
        logger.output(instanceId) {
            message = msg
            type = runType
        }
    }

    private fun error(msg: String, t: Throwable) {
        logger.output(instanceId) {
            message = "$msg\nCaused by ${t.stackTraceToString()}."
            type = PipelineRunType.fail
            status = PipelineRunStatus.error
        }
    }

    private fun findPipelineSourceTopic(pipeline: Pipeline): MutableMap<String, Topic> {
        val topicId =
            pipeline.topicId ?: throw RuntimeException("Source topic of pipeline not defined.")

        val topic = services.topic {
            findTopicById(topicId)
        } ?: throw RuntimeException("Source topic of pipeline not found.")

        return mutableMapOf(topicId to topic)
    }

    private fun shouldRun(pipeline: Pipeline, topics: MutableMap<String, Topic>, data: TriggerData): Boolean {
        if (!pipeline.conditional || pipeline.on.isNullOrEmpty()) {
            return true
        }

        val joint = pipeline.on.takeAsParameterJointOrThrow()

        return ConditionWorker(topics, data.now, mutableMapOf()).computeJoint(joint)
    }

    private fun doRun(pipeline: Pipeline, data: TriggerData) {
        log("Start to run pipeline.", PipelineRunType.start)

        try {
            val topics: MutableMap<String, Topic> = findPipelineSourceTopic(pipeline)
            pipeline.takeIf { shouldRun(it, topics, data) }?.let {
                // TODO run pipeline body
            }
        } catch (t: Throwable) {
            error("Failed to run pipeline", t)
        } finally {
            log("End of run pipeline.", PipelineRunType.end)
        }
    }


    fun run(data: TriggerData) {
        when {
            !pipeline.validated -> log("Pipeline is invalidated.", PipelineRunType.invalidate)
            !pipeline.enabled -> log("Pipeline is not enabled.", PipelineRunType.disable)
            else -> doRun(pipeline, data)
        }
    }

    override fun close() {
        services.close()
    }
}