package com.imma.persist.core.build

import com.imma.persist.core.Element
import com.imma.persist.core.Expression
import com.imma.persist.core.ExpressionOperator

class ExpressionBuilder(private val expression: Expression) {
    private fun build(operator: ExpressionOperator, right: Element? = null) {
        expression.operator = operator
        expression.right = right
    }

    fun isEmpty() {
        build(ExpressionOperator.empty)
    }

    fun isNotEmpty() {
        build(ExpressionOperator.`not-empty`)
    }

    infix fun eq(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.equals, ElementBuilder.SINGLETON.block())
    }

    infix fun notEq(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.`not-equals`, ElementBuilder.SINGLETON.block())
    }

    infix fun less(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.less, ElementBuilder.SINGLETON.block())
    }

    infix fun lessOrEq(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.`less-equals`, ElementBuilder.SINGLETON.block())
    }

    infix fun more(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.more, ElementBuilder.SINGLETON.block())
    }

    infix fun moreOrEq(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.`more-equals`, ElementBuilder.SINGLETON.block())
    }

    /**
     * in
     */
    infix fun existsIn(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.`in`, ElementBuilder.SINGLETON.block())
    }

    /**
     * not in
     */
    infix fun doesNotExistIn(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.`not-in`, ElementBuilder.SINGLETON.block())
    }

    infix fun hasText(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.`has-text`, ElementBuilder.SINGLETON.block())
    }

    infix fun contains(block: ElementBuilder.() -> Element) {
        build(ExpressionOperator.contains, ElementBuilder.SINGLETON.block())
    }
}