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
    private val pipelineLogger: PipelineLogger by lazy { PipelineLogger(pipeline, logger) }

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
            // no condition, run it
            return true
        }

        val joint = pipeline.on.takeAsParameterJointOrThrow()
        return ConditionWorker(topics, sourceData, mutableMapOf()).computeJoint(joint)
    }

    private fun doRun(pipeline: Pipeline, data: TriggerData) {
        val startTime = System.nanoTime()
        pipelineLogger.log("Start to run pipeline.", data.previous, data.now)

        try {
            val topics: MutableMap<String, Topic> = findPipelineSourceTopic(pipeline)
            @Suppress("NAME_SHADOWING")
            pipeline.takeIf {
                if (shouldRun(it, topics, data.now)) {
                    pipelineLogger.log("Pipeline ignored because of condition not reached.", PipelineRunType.ignore)
                    true
                } else {
                    false
                }
            }?.let { pipeline ->
                pipeline.stages.forEach { stage ->
                    StageWorker(StageWorkerContextBuilder().let { builder ->
                        builder.instanceId = instanceId
                        builder.pipeline = pipeline
                        builder.stage = stage
                        builder.topics = topics
                        builder.sourceData = data.now
                        builder.variables = mutableMapOf()
                        builder.services = services
                        builder.logger = logger
                        builder
                    }.build()).run()
                }
            }
        } catch (t: Throwable) {
            pipelineLogger.error("Failed to run pipeline.", t, (System.nanoTime() - startTime.toDouble()) / 1000)
        } finally {
            pipelineLogger.log(
                "End of run pipeline.",
                PipelineRunType.end,
                (System.nanoTime() - startTime.toDouble()) / 1000
            )
        }
    }

    fun run(data: TriggerData) {
        when {
            !pipeline.validated -> pipelineLogger.log(
                "Pipeline is invalidated.",
                PipelineRunType.invalidate,
                0.toDouble()
            )
            !pipeline.enabled -> pipelineLogger.log("Pipeline is not enabled.", PipelineRunType.disable, 0.toDouble())
            else -> doRun(pipeline, data)
        }
    }

    override fun close() {
        logger.close()
        services.close()
    }
}