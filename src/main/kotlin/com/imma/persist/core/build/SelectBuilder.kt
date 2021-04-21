package com.imma.persist.core.build

import com.imma.persist.core.Element
import com.imma.persist.core.Select
import com.imma.persist.core.SelectColumn

class SelectBuilder(private val select: Select) {
    private fun asColumn(element: Element): SelectColumnBuilder {
        val column = SelectColumn(element)
        select.columns += column
        return SelectColumnBuilder(column)
    }

    fun factor(factorName: String): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.factor(factorName))
    }

    fun factor(topicName: String, factorName: String): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.factor(topicName, factorName))
    }

    fun constant(value: Any?): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.value(value))
    }

    // compute functions
    fun add(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.add(block))
    }

    fun subtract(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.subtract(block))
    }

    fun multiply(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.multiply(block))
    }

    fun divide(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.divide(block))
    }

    fun modulus(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.modulus(block))
    }

    fun yearOf(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.yearOf(block))
    }

    fun halfYearOf(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.halfYearOf(block))
    }

    fun quarterOf(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.quarterOf(block))
    }

    fun monthOf(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.monthOf(block))
    }

    fun weekOfYear(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.weekOfYear(block))
    }

    fun weekOfMonth(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.weekOfMonth(block))
    }

    fun dayOfMonth(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.dayOfMonth(block))
    }

    fun dayOfWeek(block: ComputedElementsBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.dayOfWeek(block))
    }

    fun case(block: ComputedElementCaseBuilder.() -> Unit): SelectColumnBuilder {
        return asColumn(ElementBuilder.SINGLETON.case(block))
    }

    // aggregate functions
    fun count(block: ElementBuilder.() -> Element): SelectColumnBuilder {
        val element: Element = ElementBuilder.SINGLETON.block()
        return asColumn(element).count()
    }

    fun sum(block: ElementBuilder.() -> Element): SelectColumnBuilder {
        val element: Element = ElementBuilder.SINGLETON.block()
        return asColumn(element).sum()
    }

    fun avg(block: ElementBuilder.() -> Element): SelectColumnBuilder {
        val element: Element = ElementBuilder.SINGLETON.block()
        return asColumn(element).avg()
    }

    fun max(block: ElementBuilder.() -> Element): SelectColumnBuilder {
        val element: Element = ElementBuilder.SINGLETON.block()
        return asColumn(element).max()
    }

    fun min(block: ElementBuilder.() -> Element): SelectColumnBuilder {
        val element: Element = ElementBuilder.SINGLETON.block()
        return asColumn(element).min()
    }
}
