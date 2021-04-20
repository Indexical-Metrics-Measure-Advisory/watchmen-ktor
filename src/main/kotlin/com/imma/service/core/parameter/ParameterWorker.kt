package com.imma.service.core.parameter

import com.imma.model.compute.*
import com.imma.model.core.*
import com.imma.service.core.PipelineSourceData
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineVariables
import com.imma.service.core.RunContext
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
    private val sourceData: PipelineSourceData,
    private val variables: PipelineVariables
) : RunContext {
    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }

    private fun computeVariable(variable: String, parameter: ConstantParameter): Any? {
        return ParameterUtils.computeVariable(variable, parameter) { propertyName ->
            sourceData[propertyName] ?: variables[propertyName]
        }
    }

    @Suppress("DuplicatedCode")
    private fun computeConstant(parameter: ConstantParameter, shouldBe: ParameterShouldBe): Any? {
        val value = parameter.value

        return when {
            value == null -> null
            value.isEmpty() && shouldBe == ParameterShouldBe.any -> ""
            value.isEmpty() -> null
            value.isBlank() && shouldBe == ParameterShouldBe.any -> value
            value.isBlank() -> null
            else -> ParameterUtils.computeConstant(value, parameter, shouldBe) { computeVariable(it, parameter) }
        }
    }

    private fun computeTopicFactor(parameter: TopicFactorParameter, shouldBe: ParameterShouldBe): Any? {
        val (_, factor) = ParameterUtils.readTopicFactorParameter(parameter, topics) { topicId ->
            if (!isSourceTopic(topicId)) {
                throw RuntimeException("Topic of parameter[$parameter] must be source topic of pipeline.")
            }
        }

        val value = sourceData[factor.name!!]
        return when (shouldBe) {
            ParameterShouldBe.any -> value
            ParameterShouldBe.collection -> ParameterUtils.computeToCollection(value, parameter)
            ParameterShouldBe.numeric -> ParameterUtils.computeToNumeric(value, parameter)
            ParameterShouldBe.date -> ParameterUtils.computeToDate(value, parameter)
        }
    }

    private fun computeComputedToNumbers(parameters: List<Parameter>): List<BigDecimal> {
        return parameters.map { computeParameter(it, ParameterShouldBe.numeric) }.map { it as BigDecimal }
    }

    private fun computeToDate(parameter: Parameter): LocalDate? {
        return ParameterUtils.computeToDate(computeParameter(parameter), parameter)
    }

    @Suppress("DuplicatedCode")
    private fun computeComputed(parameter: ComputedParameter, shouldBe: ParameterShouldBe): Any? {
        ParameterUtils.checkSubParameters(parameter)
        ParameterUtils.checkShouldBe(parameter, shouldBe)

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
            ParameterKind.constant -> computeConstant(param as ConstantParameter, shouldBe)
            ParameterKind.computed -> computeComputed(param as ComputedParameter, shouldBe)
            else -> throw RuntimeException("Unsupported parameter[$param].")
        }
    }

    fun computeParameter(param: Parameter): Any? {
        return computeParameter(param, ParameterShouldBe.any)
    }
}