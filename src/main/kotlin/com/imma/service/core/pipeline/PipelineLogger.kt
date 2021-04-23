package com.imma.service.core.pipeline

import com.imma.service.core.EngineLoggerDelegate
import com.imma.service.core.PipelineTriggerData
import com.imma.service.core.log.RunLog
import com.imma.service.core.log.RunType

class PipelineLogger(private val context: PipelineContext) : EngineLoggerDelegate(context.logger) {
	fun start(msg: String, previous: PipelineTriggerData?, now: PipelineTriggerData) {
		logger.append {
			fillIds(this)
			message = msg
			type = RunType.start
			data = mapOf(
				"oldValue" to previous,
				"newValue" to now
			)
		}
	}

	fun ignore(msg: String, runType: RunType) {
		logger.append {
			fillIds(this)
			message = msg
			type = runType
			completeTime = 0.toDouble()
		}
	}

	override fun fillIds(log: RunLog) {
		with(context) {
			log.instanceId = instanceId
			log.pipelineId = pipeline.pipelineId
		}
	}
}
