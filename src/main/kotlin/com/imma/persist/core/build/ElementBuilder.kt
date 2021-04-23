package com.imma.persist.core.build

import com.imma.persist.core.ComputedElement
import com.imma.persist.core.ConstantElement
import com.imma.persist.core.ElementComputeOperator
import com.imma.persist.core.FactorElement

class ElementBuilder {
	companion object {
		val SINGLETON: ElementBuilder = ElementBuilder()
	}

	fun factor(factorName: String): FactorElement {
		val element = FactorElement()
		element.factorName = factorName
		return element
	}

	fun factor(topicName: String, factorName: String): FactorElement {
		val element = FactorElement()
		element.topicName = topicName
		element.factorName = factorName
		return element
	}

	fun value(value: Any?): ConstantElement {
		val element = ConstantElement()
		element.value = value
		return element
	}

	private fun compute(operator: ElementComputeOperator, block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		val element = ComputedElement()
		element.operator = operator
		ComputedElementsBuilder(element).block()
		return element
	}

	fun add(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.add, block)
	}

	fun subtract(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.subtract, block)
	}

	fun multiply(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.multiply, block)
	}

	fun divide(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.divide, block)
	}

	fun modulus(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.modulus, block)
	}

	fun yearOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`year-of`, block)
	}

	fun halfYearOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`half-year-of`, block)
	}

	fun quarterOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`quarter-of`, block)
	}

	fun monthOf(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`month-of`, block)
	}

	fun weekOfYear(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`week-of-year`, block)
	}

	fun weekOfMonth(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`week-of-month`, block)
	}

	fun dayOfMonth(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`day-of-month`, block)
	}

	fun dayOfWeek(block: ComputedElementsBuilder.() -> Unit): ComputedElement {
		return compute(ElementComputeOperator.`day-of-week`, block)
	}

	fun case(block: ComputedElementCaseBuilder.() -> Unit): ComputedElement {
		val element = ComputedElement()
		element.operator = ElementComputeOperator.`case-then`
		ComputedElementCaseBuilder(element).block()
		return element
	}
}
