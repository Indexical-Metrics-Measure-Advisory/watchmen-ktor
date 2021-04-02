package com.imma.service.core

import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.Topic
import com.imma.service.Services

class StageWorkerContextBuilder {
    var instanceId: String? = null
    var pipeline: Pipeline? = null
    var stage: PipelineStage? = null
    var topics: MutableMap<String, Topic>? = null
    var sourceData: Map<String, Any>? = null
    var variables: Map<String, Any>? = null

    var services: Services? = null
    var logger: LoggerWorker? = null

    fun build(): StageWorkerContext {
        return StageWorkerContext(
            instanceId ?: throw RuntimeException("Instance id cannot be null."),
            pipeline ?: throw RuntimeException("Pipeline cannot be null."),
            stage ?: throw RuntimeException("Stage cannot be null."),
            topics ?: throw RuntimeException("Topics cannot be null."),
            sourceData ?: mutableMapOf(),
            variables ?: mutableMapOf(),
            services ?: throw RuntimeException("Services cannot be null."),
            logger ?: throw RuntimeException("Logger worker cannot be null."),
        )
    }
}

data class StageWorkerContext(
    val instanceId: String,
    val pipeline: Pipeline,
    val stage: PipelineStage,
    val topics: MutableMap<String, Topic>,
    val sourceData: Map<String, Any>,
    val variables: Map<String, Any>,

    val services: Services,
    val logger: LoggerWorker
)

class StageWorker(private val context: StageWorkerContext) {
    private val logger: StageLogger by lazy { StageLogger(context.pipeline, context.stage, context.logger) }

    private fun shouldRun(
        stage: PipelineStage,
        topics: MutableMap<String, Topic>,
        sourceData: Map<String, Any>
    ): Boolean {
        if (!stage.conditional || stage.on.isNullOrEmpty()) {
            // no condition, run it
            return true
        }

        val joint = stage.on.takeAsParameterJointOrThrow()
        return ConditionWorker(topics, sourceData, mutableMapOf()).computeJoint(joint)
    }

    fun run() {
        val startTime = System.nanoTime()
        logger.log("Start to run stage.", PipelineRunType.start)

        try {
            context.stage.takeIf { stage ->
                if (shouldRun(stage, context.topics, context.sourceData)) {
                    logger.log("Stage ignored because of condition not reached.", PipelineRunType.ignore)
                    true
                } else {
                    false
                }
            }?.let { stage ->
                stage.units.forEach { unit ->
                    UnitWorker(UnitWorkerContextBuilder().let { builder ->
                        builder.instanceId = context.instanceId
                        builder.pipeline = context.pipeline
                        builder.stage = stage
                        builder.topics = context.topics
                        builder.sourceData = context.sourceData
                        builder.variables = context.variables
                        builder.services = context.services
                        builder.logger = context.logger
                        builder
                    }.build()).run()
                }
            }
        } catch (t: Throwable) {
            logger.error("Failed to run stage.", t, (System.nanoTime() - startTime.toDouble()) / 1000)
            throw t
        } finally {
            logger.log(
                "End of run stage.",
                PipelineRunType.end,
                (System.nanoTime() - startTime.toDouble()) / 1000
            )
        }
    }
}