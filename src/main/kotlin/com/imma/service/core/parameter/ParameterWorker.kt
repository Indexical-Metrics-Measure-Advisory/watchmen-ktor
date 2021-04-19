package com.imma.service.core.parameter

import com.imma.model.compute.*
import com.imma.model.core.*
import com.imma.service.core.PipelineSourceData
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineVariables
import com.imma.service.core.RunContext
import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

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

    private fun computeToNumeric(value: Any?, parameter: Parameter): BigDecimal? {
        return if (value == null) {
            value
        } else {
            try {
                BigDecimal(value.toString())
            } catch (t: Throwable) {
                throw RuntimeException("Cannot cast given value[$value] to numeric, which is retrieved by parameter[$parameter].")
            }
        }
    }

    private fun toDate(date: String, pattern: String, removeIrrelevantChars: Boolean = false): LocalDate {
        return if (removeIrrelevantChars) {
            LocalDate.parse(removeIrrelevantCharsFromDateString(date), DateTimeFormatter.ofPattern(pattern))
        } else {
            LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern))
        }
    }

    private fun removeIrrelevantCharsFromDateString(date: String): String {
        return date.split("").filter { it != " " && it != "-" && it != "/" }.joinToString(separator = "")
    }

    private fun computeToDate(date: String?, parameter: Parameter): LocalDate? {
        return when {
            date.isNullOrBlank() -> null
            date.length == 8 -> toDate(date, "yyyyMMdd")
            date.length == 10 -> toDate(date, "yyyyMMdd", true)
            date.length == 14 -> toDate(date.substring(0, 8), "yyyyMMddHHmmss")
            date.length >= 18 -> toDate(date.substring(0, 10), "yyyyMMddHHmmss", true)
            else -> throw RuntimeException("Cannot cast given value[$date] to date, which is computed by parameter[$parameter].")
        }
    }

    private fun computeToDate(date: Any?, parameter: Parameter): LocalDate? {
        return when (date) {
            null -> null
            is Date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            is LocalDate -> date
            is LocalDateTime -> date.toLocalDate()
            is String -> computeToDate(date, parameter)
            else -> throw RuntimeException("Cannot cast given value to date[$date], which is computed by parameter[$parameter].")
        }
    }

    private fun computeConstant(parameter: ConstantParameter, shouldBe: ParameterShouldBe): String? {
        val value = parameter.value
        if (value.isNullOrEmpty()) {
            return null
        } else if (value.isBlank()) {
            return value
        }

        // TODO variables in constant
        return value
    }

    private fun computeTopicFactor(parameter: TopicFactorParameter, shouldBe: ParameterShouldBe): Any? {
        val topicId = parameter.topicId
        if (topicId.isBlank()) {
            throw RuntimeException("Topic id of parameter[$parameter] cannot be blank.")
        }
        if (!isSourceTopic(topicId)) {
            throw RuntimeException("Topic of parameter[$parameter] must be source topic of pipeline.")
        }
        val topic = topics[topicId] ?: throw RuntimeException("Topic[$topicId] of parameter[$parameter] not found.")

        val factorId = parameter.factorId
        if (factorId.isBlank()) {
            throw RuntimeException("Factor id of parameter[$parameter] cannot be blank.")
        }
        val factor = topic.factors.find { it.factorId == factorId }
            ?: throw RuntimeException("Factor[$factorId] of parameter[$parameter] not found.")

        val value = sourceData[factor.name!!]
        return when (shouldBe) {
            ParameterShouldBe.any -> value
            ParameterShouldBe.numeric -> computeToNumeric(value, parameter)
            ParameterShouldBe.date -> computeToDate(value, parameter)
        }
    }

    private fun computeComputedToNumbers(parameters: List<Parameter>): List<BigDecimal> {
        return parameters.map { computeParameter(it, ParameterShouldBe.numeric) }.map { it as BigDecimal }
    }

    private fun computeToDate(parameter: Parameter): LocalDate? {
        return computeToDate(computeParameter(parameter), parameter)
    }

    private fun computeComputed(parameter: ComputedParameter, shouldBe: ParameterShouldBe): Any? {
        ParameterUtils.checkSubParameters(parameter)
        ParameterUtils.checkShouldBe(parameter, shouldBe)

        val parameters = parameter.parameters

        return when (parameter.type) {
            ParameterComputeType.none -> RuntimeException("Operator of parameter[$parameter] cannot be none.")
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
                val route = parameters.filter { it.on != null }.firstOrNull {
                    ConditionWorker(pipeline, topics, sourceData, variables).computeJoint(it.on!!)
                }
                if (route != null) {
                    return computeParameter(route, shouldBe)
                }

                val defaultRoute = parameters.find { it.on == null }
                if (defaultRoute != null) {
                    return computeParameter(defaultRoute, shouldBe)
                } else {
                    return null
                }
            }
        }
    }

    private fun computeParameter(param: Parameter, shouldBe: ParameterShouldBe): Any? {
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