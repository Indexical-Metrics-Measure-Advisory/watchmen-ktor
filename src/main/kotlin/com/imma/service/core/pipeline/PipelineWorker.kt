package com.imma.service.core.pipeline

import com.imma.model.core.compute.takeAsParameterJointOrThrow
import com.imma.service.core.Engine
import com.imma.service.core.EngineWorker
import com.imma.service.core.PipelineTrigger
import com.imma.service.core.createPipelineVariables
import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionWorker
import com.imma.service.core.stage.StageContext
import com.imma.service.core.stage.StageWorker

class PipelineWorker(private val context: PipelineContext) : EngineWorker() {
	private val logger: PipelineLogger by lazy { PipelineLogger(context) }

	private fun shouldRun(): Boolean {
		return context.run {
			if (!pipeline.conditional || pipeline.on.isNullOrEmpty()) {
				// no condition, run it
				return true
			}

			val joint = takeAsParameterJointOrThrow(pipeline.on)
			// no variables in pipeline prerequisite
			ConditionWorker(pipeline, topics, currentOfTriggerData, createPipelineVariables()).computeJoint(joint)
		}
	}

	private fun doRun() {
		if (shouldRun()) {
			try {
				this.markStart()
				logger.start("Start to run pipeline.", context.previousOfTriggerData, context.currentOfTriggerData)
				with(context.pipeline) {
					stages.forEach { StageWorker(StageContext(context, it)).run() }
				}

				logger.success("End of run pipeline.", this.markEnd())
			} catch (t: Throwable) {
				logger.fail("Failed to run pipeline.", t, this.markEnd())
			}
		} else {
			logger.ignore("Pipeline ignored because of condition not reached.")
		}
	}

	fun run() {
		context.pipeline.apply {
			try {
				when {
					!validated -> logger.ignore("Pipeline is invalidated.", RunType.invalidate)
					topicId.isNullOrBlank() -> logger.ignore(
						"Pipeline is invalidated because of source topic is not given.",
						RunType.invalidate
					)
					!enabled -> logger.ignore("Pipeline is not enabled.", RunType.disable)
					else -> doRun()
				}
			} finally {
				// write log
				val changes = context.logger.output(context)
				changes.forEach { Engine.run(it.first, PipelineTrigger(it.second, it.third)) }
			}
		}
	}
}