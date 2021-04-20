package com.imma.service.core.parameter

import com.imma.model.compute.*
import com.imma.model.core.Pipeline
import com.imma.service.core.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

private typealias CompareFunc = (value1: Any?, value2: Any?) -> Boolean
private typealias CompareFuncPair = Pair<CompareFunc, CompareFunc>

private val eqeq: CompareFunc = { value1, value2 -> value1 == value2 }
private val eqeqs: CompareFuncPair = eqeq to eqeq

private fun toDate(localDate: LocalDate): Date {
    return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
}

private fun toDate(localDateTime: LocalDateTime): Date {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
}

private fun toLocalDate(date: Date): LocalDate {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun toLocalDateTime(date: Date): LocalDateTime {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

@Suppress("SameParameterValue")
private fun format(date: Date, pattern: String): String {
    return DateTimeFormatter.ofPattern(pattern).format(date.toInstant())
}

private fun toSeconds(date: Date): Date {
    return Calendar.getInstance().run {
        time = date
        set(Calendar.MILLISECOND, 0)
        time
    }
}

private fun toSeconds(localDateTime: LocalDateTime): LocalDateTime {
    return localDateTime.truncatedTo(ChronoUnit.SECONDS)
}

private fun purifyYMD(date: String): String {
    return ParameterUtils.removeIrrelevantCharsFromDateString(date).substring(0, 8)
}

/**
 * condition worker for workout a boolean value.
 * which means:
 * 1. any topic in topic/factor parameter must be source topic.
 * 2. any variable in constant parameter must be source topic or can be found from variables
 *
 * @see ParameterWorker
 */
class ConditionWorker(
    private val pipeline: Pipeline,
    private val topics: PipelineTopics,
    private val sourceData: PipelineSourceData,
    private val variables: PipelineVariables = createPipelineVariables()
) : RunContext {
    private val parameterWorker: ParameterWorker by lazy { ParameterWorker(pipeline, topics, sourceData, variables) }

    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }

    private fun empty(value: Any?): Boolean {
        return value == null || (value is String && value.isEmpty())
    }

    private fun compareWhenOneNumberAtLeast(value1: Any, value2: Any, functions: CompareFuncPair): Boolean {
        return when {
            value1 is Number -> when (value2) {
                is Number -> functions.first(value1.toDouble(), value2.toDouble())
                is BigDecimal -> functions.first(value1.toDouble(), value2.toDouble())
                is BigInteger -> functions.first(value1.toDouble(), value2.toDouble())
                is String -> functions.first(value1.toDouble(), BigDecimal(value2).toDouble())
                else -> functions.first(value1.toDouble(), BigDecimal(value2.toString()).toDouble())
            }
            value1 is BigDecimal -> when (value2) {
                is Number -> functions.first(value1.toDouble(), value2.toDouble())
                is BigDecimal -> functions.first(value1, value2)
                is BigInteger -> functions.first(value1, BigDecimal(value2))
                is String -> functions.first(value1, BigDecimal(value2))
                else -> functions.first(value1, BigDecimal(value2.toString()))
            }
            value1 is BigInteger -> when (value2) {
                is Number -> functions.first(value1.toDouble(), value2.toDouble())
                is BigDecimal -> functions.first(BigDecimal(value1), value2)
                is BigInteger -> functions.first(value1, value2)
                is String -> functions.first(BigDecimal(value1), BigDecimal(value2))
                else -> functions.first(BigDecimal(value1), BigDecimal(value2.toString()))
            }
            value2 is Number -> compareWhenOneNumberAtLeast(value2, value1, functions.second to functions.first)
            else -> false
        }
    }

    private fun compareWhenOneDateAtLeast(value1: Any, value2: Any, functions: CompareFuncPair): Boolean {
        return when {
            value1 is Date -> when (value2) {
                // compare date only on Date vs String
                is String -> value2.length >= 8 && functions.first(format(value1, "yyyyMMdd"), purifyYMD(value2))
                // compare date only on Date vs LocalDate
                is LocalDate -> functions.first(toLocalDate(value1), value2)
                // compare all fields until second on Date vs LocalDateTime
                is LocalDateTime -> functions.first(toSeconds(toLocalDateTime(value1)), toSeconds(value2))
                // compare all fields until second on Date vs LocalDateTime
                is Date -> functions.first(toSeconds(value1), toSeconds(value2))
                else -> false
            }
            value1 is LocalDate -> compareWhenOneDateAtLeast(toDate(value1), value2, functions)
            value1 is LocalDateTime -> compareWhenOneDateAtLeast(toDate(value1), value2, functions)
            value2 is Date -> compareWhenOneDateAtLeast(value2, value1, functions.second to functions.first)
            value2 is LocalDate -> compareWhenOneDateAtLeast(value2, value1, functions.second to functions.first)
            value2 is LocalDateTime -> compareWhenOneDateAtLeast(value2, value1, functions.second to functions.first)
            else -> false
        }
    }

    private fun eq(value1: Any?, value2: Any?): Boolean {
        return when {
            value1 == null -> value2 == null
            value2 == null -> false
            value1 == value2 -> true
            eqeq(value1.toString(), value2.toString()) -> true
            compareWhenOneNumberAtLeast(value1, value2, eqeqs) -> true
            compareWhenOneDateAtLeast(value1, value2, eqeqs) -> true
            else -> false
        }
    }

    private fun less(value1: Any?, value2: Any?): Boolean {
        return when {
            // null value always less than not null value
            value1 == null -> value2 != null
            // not null value always not less than null value
            value2 == null -> false
//            value1 is Number && value2 is Number -> value1.toDouble() < value2.toDouble()
//            value1 is Date && value2 is Date -> value1 < value2
//            value1 is ChronoLocalDate && value2 is ChronoLocalDate -> value1 < value2
//            value1 is ChronoLocalDateTime<*> && value2 is ChronoLocalDateTime<*> -> value1 < value2
            else -> {
                // TODO
                throw RuntimeException("Less than operator is only compatible for numeric or null values, currently are [$value1] and [$value2].")
            }
        }
    }

    private fun more(value1: Any?, value2: Any?): Boolean {
        return when {
            // null value always less than not null value
            value2 == null -> value1 != null
            // not null value always not less than null value
            value1 == null -> false
//            value1 is Number && value2 is Number -> value1.toDouble() > value2.toDouble()
//            value1 is Date && value2 is Date -> value1 > value2
//            value1 is ChronoLocalDate && value2 is ChronoLocalDate -> value1 > value2
//            value1 is ChronoLocalDateTime<*> && value2 is ChronoLocalDateTime<*> -> value1 > value2
            else -> {
                // TODO
                throw RuntimeException("More than operator is only compatible for numeric or null values, currently are [$value1] and [$value2].")
            }
        }
    }

    private fun computeExpression(expression: ParameterExpression): Boolean {
        val left = { shouldBe: ParameterShouldBe ->
            parameterWorker.computeParameter(expression.left, shouldBe)
        }
        // lazy compute
        val right = { shouldBe: ParameterShouldBe ->
            expression.right.takeIf { it != null }?.run {
                parameterWorker.computeParameter(this, shouldBe)
            }
        }
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (expression.operator) {
            ParameterExpressionOperator.equals -> eq(left(ParameterShouldBe.any), right(ParameterShouldBe.any))
            ParameterExpressionOperator.`not-equals` -> !eq(left(ParameterShouldBe.any), right(ParameterShouldBe.any))
            ParameterExpressionOperator.empty -> this.empty(left(ParameterShouldBe.any))
            ParameterExpressionOperator.`not-empty` -> !this.empty(left(ParameterShouldBe.any))
            ParameterExpressionOperator.less -> this.less(left, right(ParameterShouldBe.any))
            ParameterExpressionOperator.`less-equals` -> {
                val l = left(ParameterShouldBe.any)
                val r = right(ParameterShouldBe.any)
                l == r || this.less(l, r)
            }
            ParameterExpressionOperator.more -> this.more(left(ParameterShouldBe.any), right(ParameterShouldBe.any))
            ParameterExpressionOperator.`more-equals` -> {
                val l = left(ParameterShouldBe.any)
                val r = right(ParameterShouldBe.any)
                l == r || this.more(l, r)
            }
            ParameterExpressionOperator.`in` -> {
                val l = left(ParameterShouldBe.any)
                val r = right(ParameterShouldBe.collection)
                if (r is Iterable<*>) {
                    r.any { eq(l, it) }
                } else {
                    throw throw RuntimeException("In operator is only compatible for right values are iterable, currently is [$r].")
                }
            }
            ParameterExpressionOperator.`not-in` -> {
                val l = left(ParameterShouldBe.any)
                val r = right(ParameterShouldBe.collection)
                if (r is Iterable<*>) {
                    r.all { !eq(l, it) }
                } else {
                    throw throw RuntimeException("In operator is only compatible for right values are iterable, currently is [$r].")
                }
            }
            else -> throw RuntimeException("Unsupported expression operator[${expression.operator}].")
        }
    }

    private fun computeCondition(condition: ParameterCondition): Boolean {
        return when (condition) {
            is ParameterJoint -> this.computeJoint(condition)
            is ParameterExpression -> this.computeExpression(condition)
            else -> throw RuntimeException("Unsupported condition[$condition].")
        }
    }

    fun computeJoint(joint: ParameterJoint): Boolean {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (joint.jointType) {
            ParameterJointType.and -> joint.filters.all { this.computeCondition(it) }
            ParameterJointType.or -> joint.filters.any { this.computeCondition(it) }
            else -> throw RuntimeException("Unsupported joint type[${joint.jointType}].")
        }
    }
}