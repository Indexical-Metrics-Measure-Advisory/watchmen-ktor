package com.imma.service.core.parameter

import com.imma.model.core.*
import com.imma.model.core.compute.*
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineTriggerData
import com.imma.service.core.PipelineVariables
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.WeekFields

/**
 * parameter worker for workout a constant value.
 * which means:
 * 1. any topic in topic/factor parameter must be source topic.
 * 2. any variable in constant parameter must be source topic or can be found from variables
 */
class ParameterWorker(
	private val pipeline: Pipeline,
	private val topics: PipelineTopics,
	private val sourceData: PipelineTriggerData,
	private val variables: PipelineVariables
) {
	private fun isSourceTopic(topicId: String): Boolean {
		return topicId == pipeline.topicId
	}

	private fun computeTopicFactor(parameter: TopicFactorParameter, shouldBe: ParameterShouldBe): Any? {
		val (_, factor) = ParameterKits.readTopicFactorParameter(parameter, topics) { topicId ->
			if (!isSourceTopic(topicId)) {
				throw RuntimeException("Topic of parameter[$parameter] must be source topic of pipeline.")
			}
		}

		val value = sourceData[factor.name!!]
		return when (shouldBe) {
			ParameterShouldBe.any -> value
			ParameterShouldBe.collection -> ParameterKits.computeToCollection(value, parameter)
			ParameterShouldBe.numeric -> ParameterKits.computeToNumeric(value, parameter)
			ParameterShouldBe.date -> ParameterKits.computeToDate(value, parameter)
		}
	}

	private fun computeComputedToNumbers(parameters: List<Parameter>): List<BigDecimal> {
		return parameters.map { computeParameter(it, ParameterShouldBe.numeric) }.map { it as BigDecimal }
	}

	private fun computeToDate(parameter: Parameter): LocalDate? {
		return ParameterKits.computeToDate(computeParameter(parameter), parameter)
	}

	@Suppress("DuplicatedCode")
	private fun computeComputed(parameter: ComputedParameter, shouldBe: ParameterShouldBe): Any? {
		ParameterKits.checkSubParameters(parameter)
		ParameterKits.checkShouldBe(parameter, shouldBe)

		val parameters = parameter.parameters

		return when (parameter.type) {
			ParameterComputeType.none -> throw RuntimeException("Operator of parameter[$parameter] cannot be none.")
			ParameterComputeType.add -> computeComputedToNumbers(parameters).sumOf { it }
			ParameterComputeType.subtract -> {
				val values = computeComputedToNumbers(parameters)
				return values.subList(1, values.size).fold(values[0]) { acc, v -> acc - v }
			}
			ParameterComputeType.multiply -> computeComputedToNumbers(parameters).reduce { acc, value -> acc * value }
			ParameterComputeType.divide -> {
				val values = computeComputedToNumbers(parameters)
				return values.subList(1, values.size).fold(values[0]) { acc, v -> acc / v }
			}
			ParameterComputeType.modulus -> {
				val v0 = computeParameter(parameters[0], ParameterShouldBe.numeric) as BigDecimal
				val v1 = computeParameter(parameters[1], ParameterShouldBe.numeric) as BigDecimal
				v0 % v1
			}
			ParameterComputeType.`year-of` -> computeToDate(parameters[0])?.year
			ParameterComputeType.`half-year-of` -> {
				val month = computeToDate(parameters[0])?.month?.value
				return when {
					month == null -> null
					month <= Month.JUNE.value -> HALF_YEAR_FIRST
					else -> HALF_YEAR_SECOND
				}
			}
			ParameterComputeType.`quarter-of` -> {
				val month = computeToDate(parameters[0])?.month?.value
				return when {
					month == null -> null
					month <= Month.MARCH.value -> QUARTER_FIRST
					month <= Month.JUNE.value -> QUARTER_SECOND
					month <= Month.SEPTEMBER.value -> QUARTER_THIRD
					else -> QUARTER_FOURTH
				}
			}
			ParameterComputeType.`month-of` -> computeToDate(parameters[0])?.month?.value
			ParameterComputeType.`week-of-year` -> {
				// week starts from sunday, and first week must have 7 days
				val weekFields = WeekFields.of(DayOfWeek.SUNDAY, 7)
				return computeToDate(parameters[0])?.get(weekFields.weekOfYear())
			}
			ParameterComputeType.`week-of-month` -> {
				// week starts from sunday, and first week must have 7 days
				val weekFields = WeekFields.of(DayOfWeek.SUNDAY, 7)
				return computeToDate(parameters[0])?.get(weekFields.weekOfMonth())
			}
			ParameterComputeType.`day-of-month` -> computeToDate(parameters[0])?.dayOfMonth
			ParameterComputeType.`day-of-week` -> computeToDate(parameters[0])?.dayOfWeek?.value
			ParameterComputeType.`case-then` -> {
				val route = parameters.filter { it.conditional == true && it.on != null }.firstOrNull {
					ConditionWorker(pipeline, topics, sourceData, variables).computeJoint(it.on!!)
				}
				if (route != null) {
					return computeParameter(route, shouldBe)
				}

				val defaultRoute = parameters.find { it.on == null }
				return if (defaultRoute != null) {
					computeParameter(defaultRoute, shouldBe)
				} else {
					null
				}
			}
		}
	}

	@Suppress("DuplicatedCode")
	fun computeParameter(param: Parameter, shouldBe: ParameterShouldBe): Any? {
		@Suppress("REDUNDANT_ELSE_IN_WHEN")
		return when (param.kind) {
			ParameterKind.topic -> computeTopicFactor(param as TopicFactorParameter, shouldBe)
			ParameterKind.constant -> ParameterKits.computeConstant(
				param as ConstantParameter,
				shouldBe
			) { propertyName ->
				sourceData[propertyName] ?: variables[propertyName]
			}
			ParameterKind.computed -> computeComputed(param as ComputedParameter, shouldBe)
			else -> throw RuntimeException("Unsupported parameter[$param].")
		}
	}

	fun computeParameter(param: Parameter): Any? {
		return computeParameter(param, ParameterShouldBe.any)
	}
}