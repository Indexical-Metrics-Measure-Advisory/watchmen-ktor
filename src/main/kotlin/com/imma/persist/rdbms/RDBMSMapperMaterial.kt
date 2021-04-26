package com.imma.persist.rdbms

import com.imma.persist.core.*
import com.imma.persist.defs.MapperMaterial
import com.imma.persist.defs.PersistObject

class SQLPart(val statement: String, val values: List<Any?>)

abstract class RDBMSMapperMaterial(
	entity: Any?,
	entityClass: Class<*>? = null,
	entityName: String? = null,
	private val functions: RDBMSFunctions
) : MapperMaterial(entity, entityClass, entityName) {
	fun toPersistObject(generateId: () -> Any): PersistObject {
		return this.getDef().toPersistObject(entity!!, generateId)
	}

	fun toPersistObject(): PersistObject {
		return this.getDef().toPersistObject(entity!!)
	}

	fun fromPersistObject(po: PersistObject): Any {
		return this.getDef().fromPersistObject(po)
	}

	fun generateIdFilter(): Pair<String, Any?> {
		return this.getDef().generateIdFilter(entity!!)
	}

	/**
	 * when id is not passed, use id from given entity
	 */
	fun buildIdFilter(id: String? = null): SQLPart {
		val statement = "${getIdFieldName()} = ?"
		return SQLPart(statement, listOf(id ?: getIdValue()))
	}

	/**
	 * projection for aggregate
	 */
	fun toProjection(select: Select): String {
		return select.columns.joinToString(", ") { column ->
			when (column.element) {
				is FactorElement -> fromFactorElement(column.element, ElementShouldBe.any, false)
				else -> throw RuntimeException("Only plain factor column is supported in projection, but is [$column] now.")
			}
		}
	}

	fun toUpdates(updates: Updates): SQLPart {
		val values = mutableListOf<Any?>()
		val statement = updates.parts.map {
			val factor = it.factor
			val factorName = factor.factorIdOrName
			if (factorName.isNullOrBlank()) {
				throw RuntimeException("Factor name cannot be null or blank.")
			}

			val fieldName = toFieldName(factorName)

			when (it.type) {
				FactorUpdateType.SET -> "fieldName" to it.value
				// TODO pull value from array
				FactorUpdateType.PULL -> fieldName to it.value
				// TODO push value into array
				FactorUpdateType.PUSH -> fieldName to it.value
			}
		}.joinToString(", ") { (key, value) ->
			values.add(value)
			"$key = ?"
		}
		return SQLPart(statement, values)
	}

	fun toFilter(where: Where): SQLPart {
		return fromJoint(where)
	}

	@Suppress("DuplicatedCode")
	private fun fromJoint(joint: Joint): SQLPart {
		val parts = joint.parts
		if (parts.isNullOrEmpty()) {
			throw RuntimeException("No expression under joint[$joint].")
		}

		if (parts.size == 1) {
			// only one sub part under current joint, ignore joint and return filter of sub part
			return when (val first = parts[0]) {
				is Joint -> fromJoint(first)
				is Expression -> fromExpression(first)
				else -> throw RuntimeException("Unsupported part[$first] of condition.")
			}
		}

		val type = joint.type
		val operator = if (type == JointType.and) " AND " else " OR "
		val values = mutableListOf<Any?>()
		val statement = parts.map { part ->
			when (part) {
				is Joint -> fromJoint(part)
				is Expression -> fromExpression(part)
				else -> throw RuntimeException("Unsupported part[$part] of condition.")
			}
		}.also { statementParts ->
			values.addAll(statementParts.map { it.values }.flatten())
		}.joinToString(separator = operator, prefix = "(", postfix = ")")

		return SQLPart(statement, values)
	}

	private fun fromExpression(exp: Expression): SQLPart {
		TODO()
//		val left = exp.left ?: throw RuntimeException("Left of [$exp] cannot be null.")
//		val operator = exp.operator ?: throw RuntimeException("Operator of [$exp] cannot be null.")
//
//		val right = exp.right
//		if (operator != ExpressionOperator.empty && operator != ExpressionOperator.`not-empty` && right == null) {
//			throw RuntimeException("Right of [$exp] cannot be null when operator is neither empty nor not-empty.")
//		}
//
//		return when (operator) {
//			ExpressionOperator.empty -> MF.eq(fromElement(left), null)
//			ExpressionOperator.`not-empty` -> MF.notEq(fromElement(left), null)
//			ExpressionOperator.equals -> MF.eq(fromElement(left), fromElement(right!!))
//			ExpressionOperator.`not-equals` -> MF.notEq(fromElement(left), fromElement(right!!))
//			ExpressionOperator.less -> MF.lt(fromElement(left), fromElement(right!!))
//			ExpressionOperator.`less-equals` -> MF.lte(fromElement(left), fromElement(right!!))
//			ExpressionOperator.more -> MF.gt(fromElement(left), fromElement(right!!))
//			ExpressionOperator.`more-equals` -> MF.gte(fromElement(left), fromElement(right!!))
//			ExpressionOperator.`in` -> MF.exists(fromElement(left), fromElement(right!!))
//			ExpressionOperator.`not-in` -> MF.notExists(fromElement(left), fromElement(right!!))
//			ExpressionOperator.regex -> MF.regex(fromElement(left), fromElement(right!!))
//			ExpressionOperator.contains -> MF.eq(fromElement(left), fromElement(right!!))
//		}
	}

	override fun fromComputedElement(element: ComputedElement, shouldBe: ElementShouldBe): SQLPart {
		val operator = element.operator ?: throw RuntimeException("Operator of [$element] cannot be null.")
		val elements = element.elements.also {
			if (it.size == 0) throw RuntimeException("Elements of [$element] cannot be null.")
		}
		this.checkElements(element)

		return when (operator) {
			ElementComputeOperator.add -> functions.add(elements.map { fromElement(it, ElementShouldBe.numeric) })
			ElementComputeOperator.subtract -> functions.subtract(elements.map {
				fromElement(
					it,
					ElementShouldBe.numeric
				)
			})
			ElementComputeOperator.multiply -> functions.multiply(elements.map {
				fromElement(
					it,
					ElementShouldBe.numeric
				)
			})
			ElementComputeOperator.divide -> functions.divide(elements.map { fromElement(it, ElementShouldBe.numeric) })
			ElementComputeOperator.modulus -> functions.mod(
				fromElement(elements[0], ElementShouldBe.numeric),
				fromElement(elements[1], ElementShouldBe.numeric)
			)
			ElementComputeOperator.`year-of` -> functions.year(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`half-year-of` -> functions.halfYear(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`quarter-of` -> functions.quarter(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`month-of` -> functions.month(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`week-of-year` -> functions.weekOfYear(
				fromElement(
					elements[0],
					ElementShouldBe.date
				)
			)
			ElementComputeOperator.`week-of-month` -> functions.weekOfMonth(
				fromElement(
					elements[0],
					ElementShouldBe.date
				)
			)
			ElementComputeOperator.`day-of-month` -> functions.dayOfMonth(
				fromElement(
					elements[0],
					ElementShouldBe.date
				)
			)
			ElementComputeOperator.`day-of-week` -> functions.dayOfWeek(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`case-then` -> toCaseThenMatcher(elements, shouldBe)
		}
	}

	override fun fromElement(element: Element, shouldBe: ElementShouldBe): SQLPart {
		return when (element) {
			is FactorElement -> SQLPart(fromFactorElement(element, shouldBe), listOf())
			is ConstantElement -> SQLPart("?", listOf(fromConstantElement(element, shouldBe)))
			is ComputedElement -> fromComputedElement(element, shouldBe)
			else -> throw RuntimeException("Unsupported [$element] in balanced expression.")
		}
	}

	protected open fun toCaseThenMatcher(elements: MutableList<Element>, shouldBe: ElementShouldBe): SQLPart {
		val values = mutableListOf<Any?>()
		val cases = elements.filter { it.joint != null }.map { case ->
			val condition = fromJoint(case.joint!!)
			val value = fromElement(case, shouldBe)
			values.addAll(condition.values)
			values.addAll(value.values)
			"CASE ${condition.statement} THEN ${value.statement}"
		}.joinToString(" ")

		val defaultCase = elements.find { it.joint == null }
		return if (defaultCase == null) {
			SQLPart("CASE $cases END", values)
		} else {
			val default = fromElement(defaultCase, shouldBe)
			values.addAll(default.values)
			SQLPart("CASE $cases ELSE ${default.statement} END", values)
		}
	}

	override fun toFieldNameInExpression(fieldName: String): String {
		return fieldName
	}

	override fun toFieldNameInSelection(fieldName: String): String {
		return fieldName
	}
}
