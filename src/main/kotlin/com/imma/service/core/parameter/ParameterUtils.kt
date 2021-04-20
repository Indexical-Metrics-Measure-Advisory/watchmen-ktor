package com.imma.service.core.parameter

import com.imma.model.compute.*
import com.imma.model.core.Factor
import com.imma.model.core.Topic
import com.imma.service.core.PipelineTopics
import com.imma.utils.nothing
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("EnumEntryName")
enum class ParameterShouldBe {
    any,
    numeric,
    date,
    collection
}

data class FoundFactor(val topic: Topic, val factor: Factor)

class ParameterUtils {
    companion object {
        private fun checkMinSubParameterCount(parameter: ComputedParameter, count: Int) {
            val size = parameter.parameters.size
            if (size < count) {
                throw RuntimeException("At least $count sub parameter(s) in [$parameter], but only [$size] now.")
            }
        }

        private fun checkMaxSubParameterCount(parameter: ComputedParameter, count: Int) {
            val size = parameter.parameters.size
            if (size > count) {
                throw RuntimeException("At most $count sub parameter(s) in [$parameter], but [$size] now.")
            }
        }

        fun checkSubParameters(parameter: ComputedParameter) {
            when (parameter.type) {
                ParameterComputeType.none -> nothing()
                ParameterComputeType.add -> checkMinSubParameterCount(parameter, 2)
                ParameterComputeType.subtract -> checkMinSubParameterCount(parameter, 2)
                ParameterComputeType.multiply -> checkMinSubParameterCount(parameter, 2)
                ParameterComputeType.divide -> checkMinSubParameterCount(parameter, 2)
                ParameterComputeType.modulus -> {
                    checkMinSubParameterCount(parameter, 2)
                    checkMaxSubParameterCount(parameter, 2)
                }
                ParameterComputeType.`year-of` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`half-year-of` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`quarter-of` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`month-of` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`week-of-year` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`week-of-month` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`day-of-month` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`day-of-week` -> checkMaxSubParameterCount(parameter, 1)
                ParameterComputeType.`case-then` -> {
                    checkMinSubParameterCount(parameter, 1)
                    if (parameter.parameters.count { it.on == null } > 1) {
                        throw RuntimeException("Multiple anyway routes in case-then expression of [$parameter] is not allowed.")
                    }
                }
            }
        }

        fun checkShouldBe(parameter: ComputedParameter, shouldBe: ParameterShouldBe) {
            val type = parameter.type

            when {
                type == ParameterComputeType.none -> nothing()
                type == ParameterComputeType.`case-then` -> nothing()
                shouldBe == ParameterShouldBe.any -> nothing()
                // cannot get date by computing except case-then
                shouldBe == ParameterShouldBe.date -> throw RuntimeException("Cannot get date result on parameter[$parameter].")
                shouldBe == ParameterShouldBe.numeric -> nothing()
                // cannot get collection by computing except case-then
                shouldBe == ParameterShouldBe.collection -> throw RuntimeException("Cannot get collection result on parameter[$parameter].")
            }
        }

        fun readTopicFactorParameter(
            parameter: TopicFactorParameter,
            topics: PipelineTopics,
            validOrThrow: (topicId: String) -> Unit
        ): FoundFactor {
            val topicId = parameter.topicId
            if (topicId.isBlank()) {
                throw RuntimeException("Topic id of parameter[$parameter] cannot be blank.")
            }
            validOrThrow(topicId)
            val topic = topics[topicId]
                ?: throw RuntimeException("Topic[$topicId] of parameter[$parameter] not found.")

            val factorId = parameter.factorId
            if (factorId.isBlank()) {
                throw RuntimeException("Factor id of parameter[$parameter] cannot be blank.")
            }
            val factor = topic.factors.find { it.factorId == factorId }
                ?: throw RuntimeException("Factor[$factorId] of parameter[$parameter] not found.")

            return FoundFactor(topic, factor)
        }

        fun removeIrrelevantCharsFromDateString(date: String): String {
            return date.split("").filter {
                it != " " && it != "-" && it != "/" && it != ":"
            }.joinToString(separator = "")
        }

        private fun computeToDate(date: String, pattern: String, removeIrrelevantChars: Boolean = false): LocalDate {
            return if (removeIrrelevantChars) {
                LocalDate.parse(removeIrrelevantCharsFromDateString(date), DateTimeFormatter.ofPattern(pattern))
            } else {
                LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern))
            }
        }

        private fun computeToDate(date: String?, parameter: Parameter): LocalDate? {
            val length = date?.length ?: 0
            return when {
                date.isNullOrBlank() -> null
                // format is yyyyMMdd
                length == 8 -> computeToDate(date, "yyyyMMdd")
                // format is yyyy/MM/dd, yyyy-MM-dd
                length == 10 -> computeToDate(date, "yyyyMMdd", true)
                // format is yyyyMMddHHmmss
                length == 14 -> computeToDate(date.substring(0, 8), "yyyyMMddHHmmss")
                // format is yyyyMMdd HHmmss
                length == 15 -> computeToDate(date.substring(0, 8), "yyyyMMdd HHmmss")
                // date format is yyyy/MM/dd, yyyy-MM-dd
                // time format is HH:mm:ss
                length >= 18 -> computeToDate(date.substring(0, 10), "yyyyMMddHHmmss", true)
                else -> throw RuntimeException("Cannot cast given value[$date] to date, which is computed by parameter[$parameter].")
            }
        }

        fun computeToDate(date: Any?, parameter: Parameter): LocalDate? {
            return when (date) {
                null -> null
                is Date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                is LocalDate -> date
                is LocalDateTime -> date.toLocalDate()
                is String -> computeToDate(date, parameter)
                else -> throw RuntimeException("Cannot cast given value to date[$date], which is computed by parameter[$parameter].")
            }
        }

        fun computeToNumeric(value: Any?, parameter: Parameter): BigDecimal? {
            return if (value == null) {
                value
            } else {
                try {
                    when (value) {
                        is BigDecimal -> value
                        is BigInteger -> BigDecimal(value)
                        is Int -> BigDecimal(value)
                        is Long -> BigDecimal(value)
                        else -> BigDecimal(value.toString())
                    }
                } catch (t: Throwable) {
                    throw RuntimeException("Cannot cast given value[$value] to numeric, which is retrieved by parameter[$parameter].")
                }
            }
        }

        fun computeToCollection(value: Any?, parameter: Parameter): List<Any?> {
            return when (value) {
                null -> listOf()
                is List<*> -> value
                is Array<*> -> value.toList()
                is String -> value.split(",")
                else -> throw RuntimeException("Cannot cast given value to list[$value], which is computed by parameter[$parameter].")
            }
        }

        fun computeVariable(
            variable: String,
            parameter: ConstantParameter,
            getValue: (propertyName: String) -> Any?
        ): Any? {
            if (variable.isBlank()) {
                return null
            }

            var value: Any? = null
            val parts = variable.split(".")
            for ((index, part) in parts.withIndex()) {
                value = when {
                    index == 0 -> getValue(part)
                    value == null -> null
                    value is Map<*, Any?> -> value[part]
                    else -> throw RuntimeException("Cannot retrieve value of variable[$variable], which is defined by parameter[$parameter].")
                }
            }

            return value
        }

        fun computeConstant(
            value: String,
            parameter: ConstantParameter,
            shouldBe: ParameterShouldBe,
            computeVariable: (variable: String) -> Any?
        ): Any? {
            @Suppress("RegExpRedundantEscape")
            val regexp = "([^\\{]*(\\{[^\\}]+\\})?)".toRegex()
            val values = regexp.findAll(value).map { segment ->
                val text = segment.value
                when (val braceStartIndex = text.indexOf("{")) {
                    -1 -> text
                    0 -> {
                        val variable = text.substring(1, text.length - 1).trim()
                        computeVariable(variable)
                    }
                    else -> {
                        val prefix = text.substring(0, braceStartIndex)
                        val variable = text.substring(1, text.length - 1).trim()
                        "$prefix${computeVariable(variable) ?: ""}"
                    }
                }
            }.toList()

            val v = if (values.size == 1) {
                values[0]
            } else {
                values.filterNotNull().joinToString("")
            }
            return when (shouldBe) {
                ParameterShouldBe.any -> v
                ParameterShouldBe.numeric -> computeToNumeric(v, parameter)
                ParameterShouldBe.date -> computeToDate(v, parameter)
                ParameterShouldBe.collection -> computeToCollection(v, parameter)
            }
        }
    }
}