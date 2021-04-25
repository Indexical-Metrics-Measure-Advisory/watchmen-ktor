package com.imma.persist.mango

import com.imma.persist.core.*
import com.imma.persist.defs.EntityDef
import com.imma.persist.defs.MapperMaterial
import org.bson.BsonInt32
import org.bson.Document

class MongoMapperMaterial(
	entity: Any?,
	entityClass: Class<*>? = null,
	entityName: String? = null
) : MapperMaterial(entity, entityClass, entityName) {
	private val def: MongoEntityDef = MongoEntityMapper.getDef(this)

	override fun getDef(): EntityDef {
		return this.def
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
		val filter = def.generateIdFilter(entity!!)
		return Document(filter.first, filter.second)
	}

	/**
	 * when id is not passed, use id from given entity
	 */
	fun buildIdFilter(id: String? = null): Document {
		val where = where { factor(getIdFieldName()) eq { value(id ?: getIdValue()) } }
		return toFilter(where)
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
			val factorName = factor.factorIdOrName
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

	override fun fromComputedElement(element: ComputedElement, shouldBe: ElementShouldBe): Any {
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

	override fun toFieldNameInExpression(fieldName: String): String {
		return "\$$fieldName"
	}

	override fun toFieldNameInSelection(fieldName: String): String {
		return fieldName
	}
}

class MongoMapperMaterialBuilder private constructor(private val entity: Any?) {
	private var clazz: Class<*>? = null
	private var name: String? = null

	companion object {
		fun create(entity: Any? = null): MongoMapperMaterialBuilder {
			return MongoMapperMaterialBuilder(entity)
		}
	}

	fun type(clazz: Class<*>): MongoMapperMaterialBuilder {
		this.clazz = clazz
		return this
	}

	fun name(name: String): MongoMapperMaterialBuilder {
		this.name = name
		return this
	}

	fun build(): MongoMapperMaterial {
		return MongoMapperMaterial(entity, clazz, name)
	}
}