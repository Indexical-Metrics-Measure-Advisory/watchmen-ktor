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
				PipelineStageUnitActionType.exists -> ExistsAction(context, logger).run()
				PipelineStageUnitActionType.`read-factor` -> ReadFactorAction(context, logger).run()
				PipelineStageUnitActionType.`read-row` -> ReadRowAction(context, logger).run()
				PipelineStageUnitActionType.`write-factor` -> WriteFactorAction(context, logger).run()
				PipelineStageUnitActionType.`insert-row` -> InsertRowAction(context, logger).run()
				PipelineStageUnitActionType.`merge-row` -> MergeRowAction(context, logger).run()
				PipelineStageUnitActionType.`insert-or-merge-row` -> InsertOrMergeRowAction(context, logger).run()
			}

			logger.success("End of run action.", this.markEnd())
		} catch (t: Throwable) {
			logger.fail("Failed to run action.", t, this.markEnd())
			throw t
		}
	}
}