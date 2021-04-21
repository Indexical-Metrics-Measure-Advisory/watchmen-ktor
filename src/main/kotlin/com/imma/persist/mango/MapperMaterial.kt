package com.imma.persist.mango

import com.imma.persist.core.*
import com.imma.persist.core.util.ElementKits
import org.bson.BsonInt32
import org.bson.Document

data class MapperMaterial(
	val entity: Any?,
	val entityClass: Class<*>? = null,
	val entityName: String? = null
) {
	private val def: EntityDef = EntityMapper.getDef(this)

	fun toCollectionName(): String {
		return def.toCollectionName()
	}

	fun toDocument(generateId: () -> Any): Document {
		return def.toDocument(entity!!, generateId)
	}

	fun toDocument(): Document {
		return def.toDocument(entity!!)
	}

	fun fromDocument(doc: Document): Any {
		return def.fromDocument(doc)
	}

	fun generateIdFilter(): Document {
		return def.generateIdFilter(entity!!)
	}

	/**
	 * when id is not passed, use id from given entity
	 */
	fun buildIdFilter(id: String? = null): Document {
		val where = where { factor(getIdFieldName()) eq { value(id ?: getIdValue()) } }
		return toFilter(where)
	}

	@Suppress("MemberVisibilityCanBePrivate")
	fun toFieldName(propertyOrFactorName: String): String {
		return def.toFieldName(propertyOrFactorName)
	}

	fun getIdFieldName(): String {
		return def.id.key
	}

	@Suppress("MemberVisibilityCanBePrivate")
	fun getIdValue(): Any? {
		return entity?.let { def.id.read(entity) }
	}

	/**
	 * projection for aggregate
	 */
	fun toProjection(select: Select): Document {
		return select.columns.map { column ->
			when (column.element) {
				is FactorElement -> fromFactorElement(column.element, ElementShouldBe.any, false)
				else -> throw RuntimeException("Only plain factor column is supported in projection, but is [$column] now.")
			}
		}.map {
			it to BsonInt32(1)
		}.toMap().let {
			Document("\$project", it)
		}
	}

	fun toUpdates(updates: Updates): List<Document> {
		return updates.parts.map {
			val factor = it.factor
			val factorName = factor.factorName
			if (factorName.isNullOrBlank()) {
				throw RuntimeException("Factor name cannot be null or blank.")
			}

			val fieldName = toFieldName(factorName)

			when (it.type) {
				FactorUpdateType.SET -> "\$set" to mapOf(fieldName to it.value)
				FactorUpdateType.PULL -> "\$pull" to mapOf(fieldName to it.value)
				FactorUpdateType.PUSH -> "\$push" to mapOf(fieldName to it.value)
			}
		}.map { (key, value) -> Document(key, value) }
	}

	fun toFilter(where: Where): Document {
		return Document("\$expr", fromJoint(where))
	}

	fun toMatch(where: Where): Document {
		return Document("\$match", toFilter(where))
	}

	fun toMatch(filter: Document): Document {
		return Document("\$match", filter)
	}

	fun toSkip(skipCount: Int): Document {
		return Document("\$skip", skipCount)
	}

	fun toLimit(limitCount: Int): Document {
		return Document("\$limit", limitCount)
	}

	private fun fromJoint(joint: Joint): Map<String, Any?> {
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
		val operator = if (type == JointType.and) "\$and" else "\$or"
		val sub = parts.map { part ->
			when (part) {
				is Joint -> fromJoint(part)
				is Expression -> fromExpression(part)
				else -> throw RuntimeException("Unsupported part[$part] of condition.")
			}
		}
		return mapOf(operator to sub)
	}

	private fun fromExpression(exp: Expression): Map<String, Any?> {
		val left = exp.left ?: throw RuntimeException("Left of [$exp] cannot be null.")
		val operator = exp.operator ?: throw RuntimeException("Operator of [$exp] cannot be null.")

		val right = exp.right
		if (operator != ExpressionOperator.empty && operator != ExpressionOperator.`not-empty` && right == null) {
			throw RuntimeException("Right of [$exp] cannot be null when operator is neither empty nor not-empty.")
		}

		return when (operator) {
			ExpressionOperator.empty -> MF.eq(fromElement(left), null)
			ExpressionOperator.`not-empty` -> MF.notEq(fromElement(left), null)
			ExpressionOperator.equals -> MF.eq(fromElement(left), fromElement(right!!))
			ExpressionOperator.`not-equals` -> MF.notEq(fromElement(left), fromElement(right!!))
			ExpressionOperator.less -> MF.lt(fromElement(left), fromElement(right!!))
			ExpressionOperator.`less-equals` -> MF.lte(fromElement(left), fromElement(right!!))
			ExpressionOperator.more -> MF.gt(fromElement(left), fromElement(right!!))
			ExpressionOperator.`more-equals` -> MF.gte(fromElement(left), fromElement(right!!))
			ExpressionOperator.`in` -> MF.exists(fromElement(left), fromElement(right!!))
			ExpressionOperator.`not-in` -> MF.notExists(fromElement(left), fromElement(right!!))
			ExpressionOperator.regex -> MF.regex(fromElement(left), fromElement(right!!))
			ExpressionOperator.contains -> MF.eq(fromElement(left), fromElement(right!!))
		}
	}

	private fun fromElement(element: Element, shouldBe: ElementShouldBe = ElementShouldBe.any): Any? {
		return when (element) {
			is FactorElement -> fromFactorElement(element, shouldBe)
			is ConstantElement -> fromConstantElement(element, shouldBe)
			is ComputedElement -> fromComputedElement(element, shouldBe)
			else -> throw RuntimeException("Unsupported [$element] in balanced expression.")
		}
	}

	private fun checkMinElementCount(element: ComputedElement, count: Int) {
		val size = element.elements.size
		if (size < count) {
			throw RuntimeException("At least $count element(s) in [$element], but only [$size] now.")
		}
	}

	private fun checkMaxElementCount(element: ComputedElement, count: Int) {
		val size = element.elements.size
		if (size > count) {
			throw RuntimeException("At most $count element(s) in [$element], but [$size] now.")
		}
	}

	private fun checkElements(element: ComputedElement) {
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

	private fun fromComputedElement(element: ComputedElement, shouldBe: ElementShouldBe): Any {
		val operator = element.operator ?: throw RuntimeException("Operator of [$element] cannot be null.")
		val elements = element.elements.also {
			if (it.size == 0) throw RuntimeException("Elements of [$element] cannot be null.")
		}
		this.checkElements(element)

		return when (operator) {
			ElementComputeOperator.add -> MF.add(elements.map { fromElement(it, ElementShouldBe.numeric) })
			ElementComputeOperator.subtract -> MF.subtract(elements.map { fromElement(it, ElementShouldBe.numeric) })
			ElementComputeOperator.multiply -> MF.multiply(elements.map { fromElement(it, ElementShouldBe.numeric) })
			ElementComputeOperator.divide -> MF.divide(elements.map { fromElement(it, ElementShouldBe.numeric) })
			ElementComputeOperator.modulus -> MF.mod(
				fromElement(elements[0], ElementShouldBe.numeric),
				fromElement(elements[1], ElementShouldBe.numeric)
			)
			ElementComputeOperator.`year-of` -> MF.year(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`half-year-of` -> MF.halfYear(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`quarter-of` -> MF.quarter(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`month-of` -> MF.month(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`week-of-year` -> MF.weekOfYear(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`week-of-month` -> MF.weekOfMonth(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`day-of-month` -> MF.dayOfMonth(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`day-of-week` -> MF.dayOfWeek(fromElement(elements[0], ElementShouldBe.date))
			ElementComputeOperator.`case-then` -> toCaseThenMatcher(elements, shouldBe)
		}
	}

	private fun toCaseThenMatcher(
		elements: MutableList<Element>,
		shouldBe: ElementShouldBe
	): Map<String, Map<String, Any?>> {
		val caseElements = elements.filter { it.joint != null }
		val firstThen = MF.case(MF.eq(fromJoint(elements[0].joint!!), true)).then(fromElement(elements[0], shouldBe))
		val cases = caseElements.filterIndexed { index, _ -> index != 0 }.fold(firstThen) { previousThen, element ->
			previousThen.case(MF.eq(fromJoint(element.joint!!), true)).then(fromElement(element, shouldBe))
		}
		// append default() to $switch when anyway element exists, otherwise finish it by done()
		return elements.find { it.joint == null }?.let { cases.default(fromElement(it, shouldBe)) } ?: cases.done()
	}

	/**
	 * 1. null -> null
	 * 2. shouldBe == any -> value itself
	 * 3. value is string && blank -> null
	 * 4. convert by kits
	 */
	private fun fromConstantElement(element: ConstantElement, shouldBe: ElementShouldBe): Any? {
		val value = element.value

		return when {
			value == null -> null
			shouldBe == ElementShouldBe.any -> value
			value is String && value.isBlank() -> null
			else -> ElementKits.to(value, shouldBe, element)
		}
	}

	/**
	 * topic name must same as entity name, which means only single collection operation is supported
	 */
	@Suppress("UNUSED_PARAMETER")
	private fun fromFactorElement(element: FactorElement, shouldBe: ElementShouldBe, inExp: Boolean = true): String {
		val topicName = element.topicName
		val factorName = element.factorName

		if (!topicName.isNullOrBlank() && !def.isTopicSupported(topicName)) {
			// topic name is assigned
			// and not supported by current entity
			throw RuntimeException("Unsupported topic of [$element].")
		}

		return if (def.isMultipleTopicsSupported()) {
			throw RuntimeException("Joins between multiple topics are not supported.")
		} else {
			val fieldName = toFieldName("$factorName")
			// in where, $fieldName
			// in select, fieldName
			if (inExp) "\$$fieldName" else fieldName
		}
	}
}

class MapperMaterialBuilder private constructor(private val entity: Any?) {
	private var clazz: Class<*>? = null
	private var name: String? = null

	companion object {
		fun create(entity: Any? = null): MapperMaterialBuilder {
			return MapperMaterialBuilder(entity)
		}
	}

	fun type(clazz: Class<*>): MapperMaterialBuilder {
		this.clazz = clazz
		return this
	}

	fun name(name: String): MapperMaterialBuilder {
		this.name = name
		return this
	}

	fun build(): MapperMaterial {
		return MapperMaterial(entity, clazz, name)
	}
}