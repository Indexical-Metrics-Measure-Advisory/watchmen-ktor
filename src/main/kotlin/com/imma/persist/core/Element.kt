package com.imma.persist.core

abstract class Element {
    var joint: Joint? = null
    override fun toString(): String {
        return "Element(joint=$joint)"
    }
}

class FactorElement : Element() {
    var factorName: String? = null
    var topicName: String? = null
    override fun toString(): String {
        return "FactorElement(factorName=$factorName, topicName=$topicName) ${super.toString()}"
    }
}

/**
 * not like constant parameter, constant element doesn't support variables mixin anymore.
 */
class ConstantElement : Element() {
    var value: Any? = null
    override fun toString(): String {
        return "ConstantElement(value=$value) ${super.toString()}"
    }
}

@Suppress("EnumEntryName")
enum class ElementComputeOperator {
    add,
    subtract,
    multiply,
    divide,
    modulus,
    `year-of`,
    `half-year-of`,
    `quarter-of`,
    `month-of`,
    `week-of-year`,
    `week-of-month`,
    `day-of-month`,
    `day-of-week`,
    `case-then`;
}

class ComputedElement : Element() {
    var operator: ElementComputeOperator? = null
    val elements: MutableList<Element> = mutableListOf()
    override fun toString(): String {
        return "ComputedElement(operator=$operator, elements=$elements) ${super.toString()}"
    }
}

@Suppress("EnumEntryName")
enum class ElementShouldBe {
    any,
    numeric,
    date,
    collection
}
