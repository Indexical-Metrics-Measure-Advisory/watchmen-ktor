package com.imma.service.core.action

import com.imma.model.compute.ConstantParameter
import com.imma.model.compute.takeAsParameterJointOrThrow
import com.imma.service.core.log.RunType
import com.imma.service.core.parameter.ConditionWorker
import com.imma.service.core.parameter.ParameterWorker

@Suppress("EnumEntryName")
enum class AlarmActionSeverity(val severity: String) {
    low("low"),
    medium("medium"),
    high("high"),
    critical("critical"),
}

interface AlarmConsumer {
    fun alarm(severity: AlarmActionSeverity, message: String)
}

class AlarmAction(private val context: ActionContext, private val logger: ActionLogger) {
    companion object {
        internal val CONSUMERS: MutableList<AlarmConsumer> = mutableListOf()

        /**
         * register when system starts
         */
        fun register(alarmConsumer: AlarmConsumer) {
            CONSUMERS += alarmConsumer
        }
    }

    private fun shouldRun(): Boolean {
        return context.run {
            val conditional = action["conditional"]
            val on = action["on"]

            when {
                conditional == false -> true
                !conditional.toString().toBoolean() -> true
                on == null -> true
                on !is Map<*, *> -> throw RuntimeException("Unsupported condition found in alarm action.")
                else -> ConditionWorker(topics, sourceData, variables).computeJoint(takeAsParameterJointOrThrow(on))
            }
        }
    }

    fun run() {
        if (shouldRun()) {
            val value = with(context) {
                val param = ConstantParameter(action["message"]?.toString(), false)
                val computed = ParameterWorker(topics, sourceData, variables).computeParameter(param)?.toString()
                    ?: "No Message"
                val severity = AlarmActionSeverity.values().find {
                    it.severity == action["severity"]?.toString()
                } ?: AlarmActionSeverity.medium
                CONSUMERS.parallelStream().forEach { it.alarm(severity, computed) }
                computed
            }
            logger.log(mutableMapOf("value" to value, "conditionResult" to true), RunType.process)
        } else {
            logger.log(mutableMapOf("conditionResult" to false), RunType.process)
        }
    }
}