package com.imma.service.core.stage

import com.imma.model.core.compute.takeAsParameterJointOrThrow
import com.imma.service.core.EngineWorker
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
            ConditionWorker(
                pipeline,
                topics,
                currentOfTriggerData,
                previousOfTriggerData,
                variables
            ).computeJoint(joint)
        }
    }

    fun run() {
        if (shouldRun()) {
            try {
                this.markStart()
                logger.start("Start to run stage.")

                with(context.stage) {
                    units.forEach { UnitWorker(UnitContext(context, it)).run() }
                }

                logger.success("End of run stage.", this.markEnd())
            } catch (t: Throwable) {
                logger.fail("Failed to run stage.", t, this.markEnd())
                throw t
            }
        } else {
            logger.ignore("Stage ignored because of condition not reached.")
        }
    }
}