package com.imma.persist.core.build

import com.imma.persist.core.*

class JointBuilder(val joint: Joint) {
    fun or(block: Or.() -> Unit) {
        joint.parts += Or().apply(block)
    }

    fun and(block: And.() -> Unit) {
        joint.parts += And().apply(block)
    }

    private fun asExpression(element: Element): ExpressionBuilder {
        val expression = Expression()
        expression.left = element
        joint.parts += expression
        return ExpressionBuilder(expression)
    }

    fun factor(factorName: String): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.factor(factorName))
    }

    fun factor(topicName: String, factorName: String): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.factor(topicName, factorName))
    }

    fun constant(value: Any?): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.value(value))
    }

    // compute functions
    fun add(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.add(block))
    }

    fun subtract(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.subtract(block))
    }

    fun multiply(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.multiply(block))
    }

    fun divide(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.divide(block))
    }

    fun modulus(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.modulus(block))
    }

    fun yearOf(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.yearOf(block))
    }

    fun halfYearOf(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.halfYearOf(block))
    }

    fun quarterOf(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.quarterOf(block))
    }

    fun monthOf(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.monthOf(block))
    }

    fun weekOfYear(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.weekOfYear(block))
    }

    fun weekOfMonth(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.weekOfMonth(block))
    }

    fun dayOfMonth(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.dayOfMonth(block))
    }

    fun dayOfWeek(block: ComputedElementsBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.dayOfWeek(block))
    }

    fun case(block: ComputedElementCaseBuilder.() -> Unit): ExpressionBuilder {
        return asExpression(ElementBuilder.SINGLETON.case(block))
    }
}