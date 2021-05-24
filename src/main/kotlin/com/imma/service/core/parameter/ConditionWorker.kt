package com.imma.service.core.parameter

import com.imma.model.core.Pipeline
import com.imma.model.core.compute.*
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineTriggerData
import com.imma.service.core.PipelineVariables
import com.imma.service.core.createPipelineVariables
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

private typealias CompareFunction = (value1: Any?, value2: Any?) -> Boolean
private typealias WhenIncomparable = (value1: Any?, value2: Any?) -> Boolean

private class CompareFunctions(
    val positive: CompareFunction,
    val inverse: CompareFunction,
    val incomparable: WhenIncomparable
) {
    fun inverse(): CompareFunctions {
        return CompareFunctions(inverse, positive, incomparable)
    }
}

private val eq: CompareFunctions = ({ value1: Any?, value2: Any? -> value1 == value2 }).run {
    CompareFunctions(this, this) { _, _ -> false }
}
private val throwWhenIncomparable: WhenIncomparable = { value1, value2 ->
    throw RuntimeException("More or less than operator is only compatible for comparable or null values, currently are [$value1] and [$value2].")
}

// less than, synonym for less
private val lt: CompareFunctions = CompareFunctions(
    { value1: Any?, value2: Any? ->
        when {
            value1 is Double && value2 is Double -> value1 < value2
            value1 is BigDecimal && value2 is BigDecimal -> value1 < value2
            value1 is String && value2 is String -> value1 < value2
            value1 is Date && value2 is Date -> value1 < value2
            value1 is LocalDate && value2 is LocalDate -> value1 < value2
            value1 is LocalDateTime && value2 is LocalDateTime -> value1 < value2
            else -> throwWhenIncomparable(value1, value2)
        }
    },
    { value1: Any?, value2: Any? ->
        when {
            value1 is Double && value2 is Double -> value1 > value2
            value1 is BigDecimal && value2 is BigDecimal -> value1 > value2
            value1 is String && value2 is String -> value1 > value2
            value1 is Date && value2 is Date -> value1 > value2
            value1 is LocalDate && value2 is LocalDate -> value1 > value2
            value1 is LocalDateTime && value2 is LocalDateTime -> value1 > value2
            else -> throwWhenIncomparable(value1, value2)
        }
    },
    throwWhenIncomparable
)

private val lte: CompareFunctions = CompareFunctions(
    { value1: Any?, value2: Any? ->
        when {
            value1 is Double && value2 is Double -> value1 <= value2
            value1 is BigDecimal && value2 is BigDecimal -> value1 <= value2
            value1 is String && value2 is String -> value1 <= value2
            value1 is Date && value2 is Date -> value1 <= value2
            value1 is LocalDate && value2 is LocalDate -> value1 <= value2
            value1 is LocalDateTime && value2 is LocalDateTime -> value1 <= value2
            else -> throwWhenIncomparable(value1, value2)
        }
    },
    { value1: Any?, value2: Any? ->
        when {
            value1 is Double && value2 is Double -> value1 >= value2
            value1 is BigDecimal && value2 is BigDecimal -> value1 >= value2
            value1 is String && value2 is String -> value1 >= value2
            value1 is Date && value2 is Date -> value1 >= value2
            value1 is LocalDate && value2 is LocalDate -> value1 >= value2
            value1 is LocalDateTime && value2 is LocalDateTime -> value1 >= value2
            else -> throwWhenIncomparable(value1, value2)
        }
    },
    throwWhenIncomparable
)

// greater than, synonym for more
private val gt: CompareFunctions = lt.inverse()
private val gte: CompareFunctions = lte.inverse()

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

@Suppress("SameParameterValue")
private fun format(date: LocalDate, pattern: String): String {
    return DateTimeFormatter.ofPattern(pattern).format(date)
}

@Suppress("SameParameterValue")
private fun format(dateTime: LocalDateTime, pattern: String): String {
    return DateTimeFormatter.ofPattern(pattern).format(dateTime)
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
    return ValueKits.removeIrrelevantCharsFromDateString(date).substring(0, 8)
}

private fun compareWhenOneNumberAtLeast(value1: Any, value2: Any, functions: CompareFunctions): Boolean {
    return when {
        value1 is Number -> when (value2) {
            is Number -> functions.positive(value1.toDouble(), value2.toDouble())
            is BigDecimal -> functions.positive(value1.toDouble(), value2.toDouble())
            is BigInteger -> functions.positive(value1.toDouble(), value2.toDouble())
            is String -> functions.positive(value1.toDouble(), BigDecimal(value2).toDouble())
            else -> functions.positive(value1.toDouble(), BigDecimal(value2.toString()).toDouble())
        }
        value1 is BigDecimal -> when (value2) {
            is Number -> functions.positive(value1.toDouble(), value2.toDouble())
            is BigDecimal -> functions.positive(value1, value2)
            is BigInteger -> functions.positive(value1, BigDecimal(value2))
            is String -> functions.positive(value1, BigDecimal(value2))
            else -> functions.positive(value1, BigDecimal(value2.toString()))
        }
        value1 is BigInteger -> when (value2) {
            is Number -> functions.positive(value1.toDouble(), value2.toDouble())
            is BigDecimal -> functions.positive(BigDecimal(value1), value2)
            is BigInteger -> functions.positive(value1, value2)
            is String -> functions.positive(BigDecimal(value1), BigDecimal(value2))
            else -> functions.positive(BigDecimal(value1), BigDecimal(value2.toString()))
        }
        value2 is Number -> compareWhenOneNumberAtLeast(value2, value1, functions.inverse())
        else -> functions.incomparable(value1, value2)
    }
}

private fun compareWhenOneDateAtLeast(value1: Any, value2: Any, functions: CompareFunctions): Boolean {
    return when {
        value1 is Date -> when (value2) {
            // compare date only on Date vs String
            is String -> value2.length >= 8 && functions.positive(format(value1, "yyyyMMdd"), purifyYMD(value2))
            // compare date only on Date vs LocalDate
            is LocalDate -> functions.positive(toLocalDate(value1), value2)
            // compare all fields until second on Date vs LocalDateTime
            is LocalDateTime -> functions.positive(toSeconds(toLocalDateTime(value1)), toSeconds(value2))
            // compare all fields until second on Date vs LocalDateTime
            is Date -> functions.positive(toSeconds(value1), toSeconds(value2))
            else -> functions.incomparable(value1, value2)
        }
        value1 is LocalDate -> when (value2) {
            // compare date only on Date vs String
            is String -> value2.length >= 8 && functions.positive(format(value1, "yyyyMMdd"), purifyYMD(value2))
            // compare date only on Date vs LocalDate
            is LocalDate -> functions.positive(value1, value2)
            // compare all fields until second on Date vs LocalDateTime
            is LocalDateTime -> functions.positive(value1, value2.toLocalDate())
            // compare all fields until second on Date vs LocalDateTime
            is Date -> functions.positive(value1, toLocalDate(value2))
            else -> functions.incomparable(value1, value2)
        }
        value1 is LocalDateTime -> when (value2) {
            // compare date only on Date vs String
            is String -> value2.length >= 8 && functions.positive(format(value1, "yyyyMMdd"), purifyYMD(value2))
            // compare date only on Date vs LocalDate
            is LocalDate -> functions.positive(value1.toLocalDate(), value2)
            // compare all fields until second on Date vs LocalDateTime
            is LocalDateTime -> functions.positive(toSeconds(value1), toSeconds(value2))
            // compare all fields until second on Date vs LocalDateTime
            is Date -> functions.positive(toSeconds(value1), toLocalDateTime(toSeconds(value2)))
            else -> functions.incomparable(value1, value2)
        }
        // the following check will process the situation which value1 is string
        value2 is Date -> compareWhenOneDateAtLeast(value2, value1, functions.inverse())
        value2 is LocalDate -> compareWhenOneDateAtLeast(value2, value1, functions.inverse())
        value2 is LocalDateTime -> compareWhenOneDateAtLeast(value2, value1, functions.inverse())
        else -> functions.incomparable(value1, value2)
    }
}

private fun empty(value: Any?): Boolean {
    return value == null || (value is String && value.isEmpty())
}

private fun eq(value1: Any?, value2: Any?): Boolean {
    return when {
        value1 == null -> value2 == null
        value2 == null -> false
        value1 == value2 -> true
        eq(value1.toString(), value2.toString()) -> true
        compareWhenOneNumberAtLeast(value1, value2, eq) -> true
        compareWhenOneDateAtLeast(value1, value2, eq) -> true
        else -> false
    }
}

private fun less(value1: Any?, value2: Any?): Boolean {
    return when {
        // null value always less than not null value
        value1 == null -> value2 != null
        // not null value always not less than null value
        value2 == null -> false
        compareWhenOneNumberAtLeast(value1, value2, lt) -> true
        compareWhenOneDateAtLeast(value1, value2, lt) -> true
        else -> throwWhenIncomparable(value1, value2)
    }
}

private fun lessOrEq(value1: Any?, value2: Any?): Boolean {
    return when {
        // null value less than or equals any value
        value1 == null -> true
        // non-null value always more than null value
        // in this case, value1 is non-null value, always returns false
        value2 == null -> false
        compareWhenOneNumberAtLeast(value1, value2, lte) -> true
        compareWhenOneDateAtLeast(value1, value2, lte) -> true
        else -> throwWhenIncomparable(value1, value2)
    }
}

private fun more(value1: Any?, value2: Any?): Boolean {
    return when {
        // null value always less than non-null value
        value2 == null -> value1 != null
        // null value always not more than any value
        value1 == null -> false
        compareWhenOneNumberAtLeast(value1, value2, gt) -> true
        compareWhenOneDateAtLeast(value1, value2, gt) -> true
        else -> throwWhenIncomparable(value1, value2)
    }
}

private fun moreOrEq(value1: Any?, value2: Any?): Boolean {
    return when {
        // null value less than or equals any value
        // then returns true only when value2 is null
        value1 == null -> value2 == null
        // non-null value always more than null value
        // in this case, value1 is non-null value, always returns true
        value2 == null -> true
        compareWhenOneNumberAtLeast(value1, value2, gte) -> true
        compareWhenOneDateAtLeast(value1, value2, gte) -> true
        else -> throwWhenIncomparable(value1, value2)
    }
}

private fun exists(value: Any?, values: Any?): Boolean {
    return if (values is Iterable<*>) {
        values.any { eq(value, it) }
    } else {
        eq(value, values)
    }
}

private fun notExists(value: Any?, values: Any?): Boolean {
    return if (values is Iterable<*>) {
        values.all { !eq(value, it) }
    } else {
        !eq(value, values)
    }
}

private class ParameterComputer(
    private val expression: ParameterExpression,
    private val parameterWorker: ParameterWorker
) {
    fun left(shouldBe: ParameterShouldBe = ParameterShouldBe.any): Any? {
        return parameterWorker.computeParameter(expression.left, shouldBe)
    }

    fun right(shouldBe: ParameterShouldBe = ParameterShouldBe.any): Any? {
        return expression.right.takeIf { it != null }?.run {
            parameterWorker.computeParameter(this, shouldBe)
        }
    }
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
    private val currentTriggerData: PipelineTriggerData,
    private val previousTriggerData: PipelineTriggerData?,
    private val variables: PipelineVariables = createPipelineVariables()
) {
    private val parameterWorker: ParameterWorker by lazy {
        ParameterWorker(
            pipeline,
            topics,
            currentTriggerData,
            previousTriggerData,
            variables
        )
    }

    private fun computeExpression(expression: ParameterExpression): Boolean {
        // lazy compute
        val pc = ParameterComputer(expression, parameterWorker)
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (expression.operator) {
            ParameterExpressionOperator.equals -> eq(pc.left(), pc.right())
            ParameterExpressionOperator.`not-equals` -> !eq(pc.left(), pc.right())
            ParameterExpressionOperator.empty -> empty(pc.left())
            ParameterExpressionOperator.`not-empty` -> !empty(pc.left())
            ParameterExpressionOperator.less -> less(pc.left(), pc.right())
            ParameterExpressionOperator.`less-equals` -> lessOrEq(pc.left(), pc.right())
            ParameterExpressionOperator.more -> more(pc.left(), pc.right())
            ParameterExpressionOperator.`more-equals` -> moreOrEq(pc.left(), pc.right())
            ParameterExpressionOperator.`in` -> exists(pc.left(), pc.right(ParameterShouldBe.collection))
            ParameterExpressionOperator.`not-in` -> notExists(pc.left(), pc.right(ParameterShouldBe.collection))
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