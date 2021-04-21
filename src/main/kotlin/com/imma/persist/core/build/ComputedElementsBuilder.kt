package com.imma.persist.core.build

import com.imma.persist.core.*

class ComputedElementsBuilder(private val computedElement: ComputedElement) {
    fun <E : Element> push(element: E): E {
        computedElement.elements += element
        return element
    }

    fun factor(factorName: String): FactorElement {
        return push(ElementBuilder.SINGLETON.factor(factorName))
    }

    fun factor(topicName: String, factorName: String): FactorElement {
        return push(ElementBuilder.SINGLETON.factor(topicName, factorName))
    }

    fun constant(value: Any?): ConstantElement {
        return push(ElementBuilder.SINGLETON.value(value))
    }

    // nested computed functions
    fun add(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.add(block))
    }

    fun subtract(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.subtract(block))
    }

    fun multiply(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.multiply(block))
    }

    fun divide(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.divide(block))
    }

    fun modulus(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.modulus(block))
    }

    fun yearOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.yearOf(block))
    }

    fun halfYearOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.halfYearOf(block))
    }

    fun quarterOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.quarterOf(block))
    }

    fun monthOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.monthOf(block))
    }

    fun weekOfYear(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.weekOfYear(block))
    }

    fun weekOfMonth(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.weekOfMonth(block))
    }

    fun dayOfMonth(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.dayOfMonth(block))
    }

    fun dayOfWeek(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.dayOfWeek(block))
    }

    fun case(block: ComputedElementCaseBuilder.() -> Unit): ComputedElement {
        return push(ElementBuilder.SINGLETON.case(block))
    }
}

class ComputedElementCaseBuilder(private val computedElement: ComputedElement) {
    fun `else`(block: ElementBuilder.() -> Element) {
        val element = ElementBuilder.SINGLETON.block()
        computedElement.elements += element
    }

    infix fun case(where: Where): ComputedElementThenBuilder {
        return ComputedElementThenBuilder(computedElement, where)
    }

    infix fun case(block: JointBuilder.() -> Unit): ComputedElementThenBuilder {
        val joint = where(block)
        return ComputedElementThenBuilder(computedElement, joint)
    }

    fun case(jointType: JointType, block: JointBuilder.() -> Unit): ComputedElementThenBuilder {
        val joint = where(jointType, block)
        return ComputedElementThenBuilder(computedElement, joint)
    }
}

class ComputedElementThenBuilder(private val computedElement: ComputedElement, private val joint: Joint) {
    infix fun then(block: ElementBuilder.() -> Element) {
        val element = ElementBuilder.SINGLETON.block()
        element.joint = joint
        computedElement.elements += element
    }
}
