package com.imma.service.core.action

import com.imma.service.core.EngineLoggerDelegate
import com.imma.service.core.log.RunLog

class ActionLogger(private val context: ActionContext) : EngineLoggerDelegate(context.logger) {
    override fun fillIds(log: RunLog) {
        with(context) {
            log.instanceId = instanceId
            log.pipelineId = pipeline.pipelineId
            log.stageId = stage.stageId
            log.unitId = unit.unitId
            log.actionId = action.actionId
        }
    }
}