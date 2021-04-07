package com.imma.service.core.pipeline

import com.imma.service.core.EngineLoggerDelegate
import com.imma.service.core.log.RunType
import com.imma.service.core.log.RunLog

class PipelineLogger(private val context: PipelineContext) : EngineLoggerDelegate(context.logger) {
    fun start(msg: String, previous: Map<String, Any>, now: Map<String, Any>) {
        logger.append {
            fillIds(this)
            message = msg
            type = RunType.start
            oldValue = previous
            newValue = now
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
