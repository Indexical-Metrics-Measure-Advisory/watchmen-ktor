package com.imma.service.core.action

import com.imma.model.core.PipelineStageUnitActionType
import com.imma.service.core.EngineWorker

class ActionWorker(private val context: ActionContext) : EngineWorker() {
    private val logger: ActionLogger by lazy { ActionLogger(context) }

    fun run() {
        try {
            this.markStart()
            logger.start("Start to run action.")

            when (context.action.type) {
                PipelineStageUnitActionType.alarm -> AlarmAction(context, logger).run()
                PipelineStageUnitActionType.`copy-to-memory` -> CopyToMemoryAction(context, logger).run()
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

            logger.success("End of run action.", this.markEnd())
        } catch (t: Throwable) {
            logger.fail("Failed to run action.", t, this.markEnd())
            throw t
        }
    }
}