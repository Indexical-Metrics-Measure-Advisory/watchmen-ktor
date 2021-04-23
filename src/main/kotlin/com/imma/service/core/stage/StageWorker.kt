package com.imma.service.core.stage

import com.imma.model.core.compute.takeAsParameterJointOrThrow
import com.imma.service.core.EngineWorker
import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionWorker
import com.imma.service.core.unit.UnitContext
import com.imma.service.core.unit.UnitWorker

class StageWorker(private val context: StageContext) : EngineWorker() {
	private val logger: StageLogger by lazy { StageLogger(context) }

	private fun shouldRun(): Boolean {
		return context.run {
			if (!stage.conditional || stage.on.isNullOrEmpty()) {
				// no condition, run it
				return true
			}

			val joint = takeAsParameterJointOrThrow(stage.on)
			ConditionWorker(pipeline, topics, currentOfTriggerData, variables).computeJoint(joint)
		}
	}

	fun run() {
		if (shouldRun()) {
			try {
				this.markStart()
				logger.log("Start to run stage.", RunType.start)

				with(context.stage) {
					units.forEach { UnitWorker(UnitContext(context, it)).run() }
				}

				logger.success("End of run stage.", this.markEnd())
			} catch (t: Throwable) {
				logger.fail("Failed to run stage.", t, this.markEnd())
				// IMPORTANT throw to interrupt pipeline, 2021/04/07
				throw t
			}
		} else {
			logger.log("Stage ignored because of condition not reached.", RunType.ignore)
		}
	}
}