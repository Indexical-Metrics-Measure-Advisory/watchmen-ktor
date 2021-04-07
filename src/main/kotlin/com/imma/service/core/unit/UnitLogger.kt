package com.imma.service.core.unit

import com.imma.service.core.EngineLoggerDelegate
import com.imma.service.core.log.RunLog

class UnitLogger(private val context: UnitContext) : EngineLoggerDelegate(context.logger) {
    override fun fillIds(log: RunLog) {
        with(context) {
            log.instanceId = instanceId
            log.pipelineId = pipeline.pipelineId
            log.stageId = stage.stageId
            log.unitId = unit.unitId
        }
    }
}
