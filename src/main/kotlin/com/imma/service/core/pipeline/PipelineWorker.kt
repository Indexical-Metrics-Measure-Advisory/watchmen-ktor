package com.imma.service.core.pipeline

import com.imma.model.core.Pipeline
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
	private val pipelineLogger: PipelineLogger by lazy { PipelineLogger(context) }

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
				pipelineLogger.start(
					"Start to run pipeline.",
					context.previousOfTriggerData,
					context.currentOfTriggerData
				)
				with(context.pipeline) {
					stages.forEach { StageWorker(StageContext(context, it)).run() }
				}

				pipelineLogger.success("End of run pipeline.", this.markEnd())
			} catch (t: Throwable) {
				pipelineLogger.fail("Failed to run pipeline.", t, this.markEnd())
			}
		} else {
			pipelineLogger.ignore("Pipeline ignored because of condition not reached.")
		}
	}

	fun run() {
		with(context) {
			try {
				pipeline.apply {
					when {
						!validated -> pipelineLogger.ignore("Pipeline is invalidated.", RunType.invalidate)
						topicId.isNullOrBlank() -> pipelineLogger.ignore(
							"Pipeline is invalidated because of source topic is not given.",
							RunType.invalidate
						)
						!enabled -> pipelineLogger.ignore("Pipeline is not enabled.", RunType.disable)
						else -> doRun()
					}
				}
			} finally {
				// write log
				val changes = logger.output(context)

				// TODO pipelines definition should be retrieved from memory cache
				val pipelines: MutableMap<String, List<Pipeline>> = mutableMapOf()
				changes.map {
					val topicId = it.first
					if (pipelines[topicId] == null) {
						pipelines[topicId] = services.pipeline { listPipelines(topicId) }
					}
					it
				}.forEach { change ->
					// TODO next pipelines trigger should be parallel
					val (topicId, oldValue, newValue) = change
					pipelines[topicId]?.forEach { Engine.run(it, PipelineTrigger(oldValue, newValue)) }
				}
			}
		}
	}
}