package com.imma.service.core.parameter

import com.imma.model.compute.*
import com.imma.model.core.Topic
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoLocalDateTime
import java.util.*

class ConditionWorker(
    private val topics: MutableMap<String, Topic>,
    private val sourceData: Map<String, Any>,
    private val variables: MutableMap<String, Any> = mutableMapOf()
) {
    private val parameterWorker: ParameterWorker by lazy { ParameterWorker(topics, sourceData, variables) }

    private fun empty(value: Any?): Boolean {
        return value == null || (value is String && value.isEmpty())
    }

    private fun eq(value1: Any?, value2: Any?): Boolean {
        return when {
            value1 == null -> value2 == null
            value2 == null -> false
            value1 is Number && value2 is Number -> value1.toDouble() == value2.toDouble()
            value1 == value2 -> true
            else -> value1.toString() == value2.toString()
        }
    }

    private fun less(value1: Any?, value2: Any?): Boolean {
        return when {
            // null value always less than not null value
            value1 == null -> value2 != null
            // not null value always not less than null value
            value2 == null -> false
            value1 is Number && value2 is Number -> value1.toDouble() < value2.toDouble()
            value1 is Date && value2 is Date -> value1 < value2
            value1 is ChronoLocalDate && value2 is ChronoLocalDate -> value1 < value2
            value1 is ChronoLocalDateTime<*> && value2 is ChronoLocalDateTime<*> -> value1 < value2
            else -> throw RuntimeException("Less than operator is only compatible for numeric or null values, currently are [$value1] and [$value2].")
        }
    }

    private fun more(value1: Any?, value2: Any?): Boolean {
        return when {
            // null value always less than not null value
            value2 == null -> value1 != null
            // not null value always not less than null value
            value1 == null -> false
            value1 is Number && value2 is Number -> value1.toDouble() > value2.toDouble()
            value1 is Date && value2 is Date -> value1 > value2
            value1 is ChronoLocalDate && value2 is ChronoLocalDate -> value1 > value2
            value1 is ChronoLocalDateTime<*> && value2 is ChronoLocalDateTime<*> -> value1 > value2
            else -> throw RuntimeException("More than operator is only compatible for numeric or null values, currently are [$value1] and [$value2].")
        }
    }

    private fun computeExpression(expression: ParameterExpression): Boolean {
        val left = parameterWorker.computeParameter(expression.left)
        // lazy compute
        val right = { parameterWorker.computeParameter(expression.right) }
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (expression.operator) {
            ParameterExpressionOperator.equals -> eq(left, right())
            ParameterExpressionOperator.`not-equals` -> !eq(left, right())
            ParameterExpressionOperator.empty -> this.empty(left)
            ParameterExpressionOperator.`not-empty` -> !this.empty(left)
            ParameterExpressionOperator.less -> this.less(left, right())
            ParameterExpressionOperator.`less-equals` -> {
                val r = right()
                left == r || this.less(left, r)
            }
            ParameterExpressionOperator.more -> this.more(left, right())
            ParameterExpressionOperator.`more-equals` -> {
                val r = right()
                left == r || this.more(left, r)
            }
            ParameterExpressionOperator.`in` -> {
                val r = right()
                if (r is Iterable<*>) {
                    r.any { eq(left, right()) }
                } else {
                    throw throw RuntimeException("In operator is only compatible for right values are iterable, currently is [$r].")
                }
            }
            ParameterExpressionOperator.`not-in` -> {
                val r = right()
                if (r is Iterable<*>) {
                    r.all { !eq(left, right()) }
                } else {
                    throw throw RuntimeException("In operator is only compatible for right values are iterable, currently is [$r].")
                }
            }
            else -> throw RuntimeException("Unsupported expression[$expression].")
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
            else -> throw RuntimeException("Unsupported joint[$joint].")
        }
    }
}