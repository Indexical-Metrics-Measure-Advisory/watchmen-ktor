package com.imma.persist.defs

import com.imma.persist.core.*
import com.imma.persist.core.util.ElementKits

abstract class MapperMaterial(
	val entity: Any?,
	val entityClass: Class<*>? = null,
	val entityName: String? = null
) {
	abstract fun getDef(): EntityDef

	@Suppress("MemberVisibilityCanBePrivate")
	fun toFieldName(propertyOrFactorName: String): String {
		return this.getDef().toFieldName(propertyOrFactorName)
	}

	fun getIdFieldName(): String {
		return this.toFieldName(getDef().getId().key)
	}

	@Suppress("MemberVisibilityCanBePrivate")
	fun getIdValue(): Any? {
		return entity?.let { getDef().getId().read(entity) }
	}

	fun toCollectionName(): String {
		return this.getDef().toCollectionName()
	}

	/**
	 * 1. null -> null
	 * 2. shouldBe == any -> value itself
	 * 3. value is string && blank -> null
	 * 4. convert by kits
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	protected fun fromConstantElement(element: ConstantElement, shouldBe: ElementShouldBe): Any? {
		val value = element.value

		return when {
			value == null -> null
			shouldBe == ElementShouldBe.any -> value
			value is String && value.isBlank() -> null
			else -> ElementKits.to(value, shouldBe, element)
		}
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun fromFactorElement(
		element: FactorElement,
		@Suppress("UNUSED_PARAMETER")
		shouldBe: ElementShouldBe,
		inExp: Boolean = true
	): String {
		val topicIdOrName = element.topicIdOrName
		val factorIdOrName = element.factorIdOrName

		if (!topicIdOrName.isNullOrBlank() && !this.getDef().isTopicSupported(topicIdOrName)) {
			// topic name is assigned
			// and not supported by current entity
			throw RuntimeException("Unsupported topic of [$element].")
		}

		return if (this.getDef().isMultipleTopicsSupported()) {
			throw RuntimeException("Joins between multiple topics are not supported.")
		} else {
			val fieldName = toFieldName("$factorIdOrName")
			// in where, $fieldName
			// in select, fieldName
			if (inExp) {
				toFieldNameInExpression(fieldName)
			} else {
				toFieldNameInSelection(fieldName)
			}
		}
	}

	protected abstract fun toFieldNameInExpression(fieldName: String): String

	protected abstract fun toFieldNameInSelection(fieldName: String): String

	protected abstract fun fromComputedElement(element: ComputedElement, shouldBe: ElementShouldBe): Any

	protected fun fromElement(element: Element, shouldBe: ElementShouldBe = ElementShouldBe.any): Any? {
		return when (element) {
			is FactorElement -> fromFactorElement(element, shouldBe)
			is ConstantElement -> fromConstantElement(element, shouldBe)
			is ComputedElement -> fromComputedElement(element, shouldBe)
			else -> throw RuntimeException("Unsupported [$element] in balanced expression.")
		}
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun checkMinElementCount(element: ComputedElement, count: Int) {
		val size = element.elements.size
		if (size < count) {
			throw RuntimeException("At least $count element(s) in [$element], but only [$size] now.")
		}
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun checkMaxElementCount(element: ComputedElement, count: Int) {
		val size = element.elements.size
		if (size > count) {
			throw RuntimeException("At most $count element(s) in [$element], but [$size] now.")
		}
	}

	protected fun checkElements(element: ComputedElement) {
		when (element.operator) {
			ElementComputeOperator.add -> checkMinElementCount(element, 2)
			ElementComputeOperator.subtract -> checkMinElementCount(element, 2)
			ElementComputeOperator.multiply -> checkMinElementCount(element, 2)
			ElementComputeOperator.divide -> checkMinElementCount(element, 2)
			ElementComputeOperator.modulus -> {
				checkMinElementCount(element, 2)
				checkMaxElementCount(element, 2)
			}
			ElementComputeOperator.`year-of` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`half-year-of` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`quarter-of` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`month-of` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`week-of-year` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`week-of-month` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`day-of-month` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`day-of-week` -> checkMaxElementCount(element, 1)
			ElementComputeOperator.`case-then` -> {
				checkMinElementCount(element, 1)
				if (element.elements.count { it.joint == null } > 1) {
					throw RuntimeException("Multiple anyway routes in case-then expression of [$element] is not allowed.")
				}
			}
		}
	}
}