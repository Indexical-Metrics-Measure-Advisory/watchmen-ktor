package com.imma.service.core.unit

import com.imma.model.core.compute.takeAsParameterJointOrThrow
import com.imma.service.core.EngineWorker
import com.imma.service.core.action.ActionContext
import com.imma.service.core.action.ActionWorker
import com.imma.service.core.parameter.ConditionWorker

class UnitWorker(private val context: UnitContext) : EngineWorker() {
	private val logger: UnitLogger by lazy { UnitLogger(context) }

	private fun shouldRun(context: UnitContext): Boolean {
		return context.run {
			if (!unit.conditional || unit.on.isNullOrEmpty()) {
				// no condition, run it
				return true
			}

			val joint = takeAsParameterJointOrThrow(unit.on)
			ConditionWorker(pipeline, topics, currentOfTriggerData, variables).computeJoint(joint)
		}
	}

	fun run() {
		val loopVariableName = context.unit.loopVariableName
		if (!loopVariableName.isNullOrBlank()) {
			// run loop
			when (val values = context.variables[loopVariableName]) {
				is Collection<Any?> -> values.forEachIndexed { index, value ->
					this.doRun(context.buildLoopContext(loopVariableName, index))
				}
				is Array<*> -> values.forEachIndexed { index, value ->
					this.doRun(context.buildLoopContext(loopVariableName, index))
				}
				else -> this.doRun(context)
			}
		} else {
			// no loop
			this.doRun(context)
		}
	}

	private fun doRun(context: UnitContext) {
		if (this.shouldRun(context)) {
			try {
				this.markStart()
				logger.start("Start to run unit.")

				with(context.unit) {
					`do`.forEach { ActionWorker(ActionContext(context, it)).run() }
				}

				logger.success("End of run unit.", this.markEnd())
			} catch (t: Throwable) {
				logger.fail("Failed to run unit.", t, this.markEnd())
				throw t
			}
		} else {
			logger.ignore("Unit ignored because of condition not reached.")
		}
	}
}