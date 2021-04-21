package com.imma.service.core.unit

import com.imma.model.core.compute.takeAsParameterJointOrThrow
import com.imma.service.core.EngineWorker
import com.imma.service.core.action.ActionContext
import com.imma.service.core.action.ActionWorker
import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionWorker

class UnitWorker(private val context: UnitContext) : EngineWorker() {
    private val logger: UnitLogger by lazy { UnitLogger(context) }

    private fun shouldRun(): Boolean {
        return context.run {
            if (!unit.conditional || unit.on.isNullOrEmpty()) {
                // no condition, run it
                return true
            }

            val joint = takeAsParameterJointOrThrow(unit.on)
            ConditionWorker(pipeline, topics, sourceData, mutableMapOf()).computeJoint(joint)
        }
    }

    fun run() {
        if (this.shouldRun()) {
            try {
                this.markStart()
                logger.log("Start to run unit.", RunType.start)

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