package com.imma.service.core.action

import com.imma.model.core.compute.ConstantParameter
import com.imma.model.core.compute.takeAsParameterJointOrThrow
import com.imma.service.core.parameter.ConditionWorker
import com.imma.service.core.parameter.ParameterWorker
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
	private val systemLogger: Logger = LoggerFactory.getLogger(this.javaClass)

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
				else -> ConditionWorker(pipeline, topics, currentOfTriggerData, variables).computeJoint(
					takeAsParameterJointOrThrow(on)
				)
			}
		}
	}

	fun run() {
		if (shouldRun()) {
			val value = with(context) {
				val param = ConstantParameter(action["message"]?.toString(), false)
				val severity = AlarmActionSeverity.values().find {
					it.severity == action["severity"]?.toString()
				} ?: AlarmActionSeverity.medium
				ParameterWorker(pipeline, topics, currentOfTriggerData, variables)
					.computeParameter(param)?.toString() ?: "No Message"
					.also { message ->
						CONSUMERS.forEach { consumer ->
							try {
								consumer.alarm(severity, message)
							} catch (t: Throwable) {
								// ignore and write throwable to system log
								systemLogger.error("Error occurred on alarm consumer[${consumer.javaClass}]", t)
							}
						}
					}
			}
			logger.log("value" to value, "conditionResult" to true)
		} else {
			logger.log("conditionResult" to false)
		}
	}
}