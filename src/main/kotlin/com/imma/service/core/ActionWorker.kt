package com.imma.service.core

import com.imma.model.core.*
import com.imma.service.Services

class ActionWorkerContextBuilder {
    var instanceId: String? = null
    var pipeline: Pipeline? = null
    var stage: PipelineStage? = null
    var unit: PipelineStageUnit? = null
    var action: PipelineStageUnitAction? = null
    var topics: MutableMap<String, Topic>? = null
    var sourceData: Map<String, Any>? = null
    var variables: Map<String, Any>? = null

    var services: Services? = null
    var logger: LoggerWorker? = null

    fun build(): ActionWorkerContext {
        return ActionWorkerContext(
            instanceId ?: throw RuntimeException("Instance id cannot be null."),
            pipeline ?: throw RuntimeException("Pipeline cannot be null."),
            stage ?: throw RuntimeException("Stage cannot be null."),
            unit ?: throw RuntimeException("Unit cannot be null."),
            action ?: throw RuntimeException("Action cannot be null."),
            topics ?: throw RuntimeException("Topics cannot be null."),
            sourceData ?: throw RuntimeException("Source data cannot be null."),
            variables ?: throw RuntimeException("Variables cannot be null."),
            services ?: throw RuntimeException("Services cannot be null."),
            logger ?: throw RuntimeException("Logger worker cannot be null."),
        )
    }
}

data class ActionWorkerContext(
    val instanceId: String,
    val pipeline: Pipeline,
    val stage: PipelineStage,
    val unit: PipelineStageUnit,
    val action: PipelineStageUnitAction,
    val topics: MutableMap<String, Topic>,
    val sourceData: Map<String, Any>,
    val variables: Map<String, Any>,

    val services: Services,
    val logger: LoggerWorker
)

class ActionWorker(private val context: ActionWorkerContext) {
    private val logger: ActionLogger by lazy {
        ActionLogger(
            context.pipeline,
            context.stage,
            context.unit,
            context.action,
            context.logger
        )
    }

    fun run() {
        val startTime = System.nanoTime()
        logger.log("Start to run action.", PipelineRunType.start)

        try {
            // TODO run action
            val action = context.action
            when (action.type) {
                PipelineStageUnitActionType.alarm -> {
                }
                PipelineStageUnitActionType.`copy-to-memory` -> {
                }
                PipelineStageUnitActionType.exists -> {
                }
                PipelineStageUnitActionType.`read-factor` -> {
                }
                PipelineStageUnitActionType.`read-row` -> {
                }
                PipelineStageUnitActionType.`write-factor` -> {
                }
                PipelineStageUnitActionType.`insert-row` -> {
                }
                PipelineStageUnitActionType.`merge-row` -> {
                }
                PipelineStageUnitActionType.`insert-or-merge-row` -> {
                }
            }
        } catch (t: Throwable) {
            logger.error("Failed to run action.", t, (System.nanoTime() - startTime.toDouble()) / 1000)
            throw t
        } finally {
            logger.log(
                "End of run action.",
                PipelineRunType.end,
                (System.nanoTime() - startTime.toDouble()) / 1000
            )
        }
    }
}