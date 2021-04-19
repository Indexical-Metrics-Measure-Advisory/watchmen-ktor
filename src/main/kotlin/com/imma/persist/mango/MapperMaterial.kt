package com.imma.persist.mango

import com.imma.persist.core.*
import com.imma.utils.neverOccur
import org.bson.BsonInt32
import org.bson.Document
import org.jetbrains.kotlin.utils.keysToMap

data class MapperMaterial(
    val entity: Any?,
    val entityClass: Class<*>? = null,
    val entityName: String? = null
) {
    private val def: EntityDef = EntityMapper.getDef(this)

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
                is FactorElement -> fromFactorElement(column.element, false)
                else -> throw RuntimeException("Only plain factor column is supported in projection, but is [$column] now.")
            }
        }.keysToMap { BsonInt32(1) }
            .let { Document("\$project", it) }
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

        return when {
            operator == ExpressionOperator.empty -> mapOf("\$eq" to listOf(fromElement(left), null))
            operator == ExpressionOperator.`not-empty` -> mapOf("\$ne" to listOf(fromElement(left), null))
            exp.right == null -> throw RuntimeException("Right of [$exp] cannot be null when operator is neither empty nor not-empty.")
            else -> fromBalancedExpression(exp)
        }
    }

    /**
     * only handle expression which already checked as balanced.
     * otherwise throw never occur error
     */
    private fun fromBalancedExpression(exp: Expression): Map<String, Any?> {
        val sign = when (exp.operator) {
            null -> neverOccur()
            ExpressionOperator.equals -> "\$eq"
            ExpressionOperator.`not-equals` -> "\$ne"
            ExpressionOperator.less -> "\$lt"
            ExpressionOperator.`less-equals` -> "\$lte"
            ExpressionOperator.more -> "\$gt"
            ExpressionOperator.`more-equals` -> "\$gte"
            ExpressionOperator.`in` -> "\$in"
            ExpressionOperator.`not-in` -> "\$nin"
            ExpressionOperator.regex -> "\$regex"
            ExpressionOperator.contains -> "\$eq"
            ExpressionOperator.empty -> neverOccur()
            ExpressionOperator.`not-empty` -> neverOccur()
        }

        return mapOf(sign to listOf(fromElement(exp.left!!), fromElement(exp.right!!)))
    }

    private fun fromElement(element: Element): Any? {
        return when (element) {
            is FactorElement -> fromFactorElement(element)
            is ConstantElement -> fromConstantElement(element)
            is ComputedElement -> fromComputedElement(element)
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

    private fun fromComputedElement(element: ComputedElement): Any {
        val operator = element.operator ?: throw RuntimeException("Operator of [$element] cannot be null.")
        val elements = element.elements.also {
            if (it.size == 0) throw RuntimeException("Elements of [$element] cannot be null.")
        }
        this.checkElements(element)

        return when (operator) {
            ElementComputeOperator.add -> MF.add(elements.map { fromElement(it) })
            ElementComputeOperator.subtract -> MF.subtract(elements.map { fromElement(it) })
            ElementComputeOperator.multiply -> MF.multiply(elements.map { fromElement(it) })
            ElementComputeOperator.divide -> MF.divide(elements.map { fromElement(it) })
            ElementComputeOperator.modulus -> MF.mod(fromElement(elements[0]), fromElement(elements[1]))
            ElementComputeOperator.`year-of` -> MF.year(fromElement(elements[0]))
            ElementComputeOperator.`half-year-of` -> MF.halfYear(elements[0])
            ElementComputeOperator.`quarter-of` -> MF.quarter(elements[0])
            ElementComputeOperator.`month-of` -> MF.month(fromElement(elements[0]))
            ElementComputeOperator.`week-of-year` -> MF.weekOfYear(fromElement(elements[0]))
            ElementComputeOperator.`week-of-month` -> MF.weekOfMonth(fromElement(elements[0]))
            ElementComputeOperator.`day-of-month` -> MF.dayOfMonth(fromElement(elements[0]))
            ElementComputeOperator.`day-of-week` -> MF.dayOfWeek(fromElement(elements[0]))
            ElementComputeOperator.`case-then` -> toCaseThenMatcher(elements)
        }
    }

    private fun toCaseThenMatcher(elements: MutableList<Element>): Map<String, Map<String, Any?>> {
        val caseElements = elements.filter { it.joint != null }
        val firstThen = MF.case(MF.equal(fromJoint(elements[0].joint!!), true)).then(fromElement(elements[0]))
        val cases = caseElements.filterIndexed { index, _ -> index != 0 }.fold(firstThen) { previousThen, element ->
            previousThen.case(MF.equal(fromJoint(element.joint!!), true)).then(fromElement(element))
        }
        // append default() to $switch when anyway element exists, otherwise finish it by done()
        return elements.find { it.joint == null }?.let { cases.default(fromElement(it)) } ?: cases.done()
    }

    private fun fromConstantElement(element: ConstantElement): String? {
        val value = element.value
        // TODO variables in constant
        return value?.toString()
    }

    /**
     * topic name must same as entity name, which means only single collection operation is supported
     */
    private fun fromFactorElement(element: FactorElement, inExp: Boolean = true): String {
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