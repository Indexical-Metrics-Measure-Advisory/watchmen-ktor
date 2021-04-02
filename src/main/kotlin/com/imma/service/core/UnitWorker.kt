package com.imma.service.core

import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.PipelineStageUnit
import com.imma.model.core.Topic
import com.imma.service.Services

class UnitWorkerContextBuilder {
    var instanceId: String? = null
    var pipeline: Pipeline? = null
    var stage: PipelineStage? = null
    var unit: PipelineStageUnit? = null
    var topics: MutableMap<String, Topic>? = null
    var sourceData: Map<String, Any>? = null
    var variables: Map<String, Any>? = null

    var services: Services? = null
    var logger: LoggerWorker? = null

    fun build(): UnitWorkerContext {
        return UnitWorkerContext(
            instanceId ?: throw RuntimeException("Instance id cannot be null."),
            pipeline ?: throw RuntimeException("Pipeline cannot be null."),
            stage ?: throw RuntimeException("Stage cannot be null."),
            unit ?: throw RuntimeException("Unit cannot be null."),
            topics ?: throw RuntimeException("Topics cannot be null."),
            sourceData ?: throw RuntimeException("Source data cannot be null."),
            variables ?: throw RuntimeException("Variables cannot be null."),
            services ?: throw RuntimeException("Services cannot be null."),
            logger ?: throw RuntimeException("Logger worker cannot be null."),
        )
    }
}

data class UnitWorkerContext(
    val instanceId: String,
    val pipeline: Pipeline,
    val stage: PipelineStage,
    val unit: PipelineStageUnit,
    val topics: MutableMap<String, Topic>,
    val sourceData: Map<String, Any>,
    val variables: Map<String, Any>,

    val services: Services,
    val logger: LoggerWorker
)

class UnitWorker(private val context: UnitWorkerContext) {
    private val logger: UnitLogger by lazy { UnitLogger(context.pipeline, context.stage, context.unit, context.logger) }

    private fun shouldRun(
        unit: PipelineStageUnit,
        topics: MutableMap<String, Topic>,
        sourceData: Map<String, Any>
    ): Boolean {
        if (!unit.conditional || unit.on.isNullOrEmpty()) {
            // no condition, run it
            return true
        }

        val joint = unit.on.takeAsParameterJointOrThrow()
        return ConditionWorker(topics, sourceData, mutableMapOf()).computeJoint(joint)
    }

    fun run() {
        val startTime = System.nanoTime()
        logger.log("Start to run unit.", PipelineRunType.start)

        try {
            context.unit.takeIf { unit ->
                if (shouldRun(unit, context.topics, context.sourceData)) {
                    logger.log("Unit ignored because of condition not reached.", PipelineRunType.ignore)
                    true
                } else {
                    false
                }
            }?.let { unit ->
                unit.`do`.forEach { action ->
                    ActionWorker(ActionWorkerContextBuilder().let { builder ->
                        builder.instanceId = context.instanceId
                        builder.pipeline = context.pipeline
                        builder.stage = context.stage
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
            logger.error("Failed to run unit.", t, (System.nanoTime() - startTime.toDouble()) / 1000)
        } finally {
            logger.log(
                "End of run unit.",
                PipelineRunType.end,
                (System.nanoTime() - startTime.toDouble()) / 1000
            )
        }
    }
}