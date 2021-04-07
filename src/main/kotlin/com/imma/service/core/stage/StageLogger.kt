package com.imma.service.core.stage

import com.imma.service.core.EngineLoggerDelegate
import com.imma.service.core.log.RunLog

class StageLogger(private val context: StageContext) : EngineLoggerDelegate(context.logger) {
    override fun fillIds(log: RunLog) {
        with(context) {
            log.instanceId = instanceId
            log.pipelineId = pipeline.pipelineId
            log.stageId = stage.stageId
        }
    }
}
