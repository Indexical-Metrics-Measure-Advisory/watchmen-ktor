package com.imma.service.core.pipeline

import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.service.core.EngineWorker
import com.imma.service.core.TriggerData
import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionWorker
import com.imma.service.core.stage.StageContext
import com.imma.service.core.stage.StageWorker

class PipelineWorker(private val context: PipelineContext) : EngineWorker() {
    private val logger: PipelineLogger by lazy { PipelineLogger(context) }

    private fun shouldRun(sourceData: Map<String, Any>): Boolean {
        return context.run {
            if (!pipeline.conditional || pipeline.on.isNullOrEmpty()) {
                // no condition, run it
                return true
            }

            val joint = takeAsParameterJointOrThrow(pipeline.on)
            ConditionWorker(pipeline, topics, sourceData, mutableMapOf()).computeJoint(joint)
        }
    }

    private fun doRun(data: TriggerData) {
        if (shouldRun(data.now)) {
            try {
                this.markStart()
                logger.start("Start to run pipeline.", data.previous, data.now)

                with(context.pipeline) {
                    stages.forEach { StageWorker(StageContext(context, it, data.now)).run() }
                }

                logger.success("End of run pipeline.", this.markEnd())
            } catch (t: Throwable) {
                logger.fail("Failed to run pipeline.", t, this.markEnd())
            }
        } else {
            logger.log("Pipeline ignored because of condition not reached.", RunType.ignore)
        }
    }

    fun run(data: TriggerData) {
        context.pipeline.apply {
            try {
                when {
                    !validated -> logger.ignore("Pipeline is invalidated.", RunType.invalidate)
                    !enabled -> logger.ignore("Pipeline is not enabled.", RunType.disable)
                    else -> doRun(data)
                }
            } finally {
                // write log
                context.logger.output()
            }
        }
    }
}