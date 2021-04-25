package com.imma.persist.defs

import com.imma.model.EntityColumns
import com.imma.model.core.Factor
import com.imma.model.core.FactorType
import com.imma.model.core.Topic
import com.imma.model.core.compute.ValueKits

class DynamicFactorDef(val factor: Factor, type: EntityFieldType) : EntityFieldDef(factor.name!!, type) {
	override fun read(entity: Any): Any? {
		if (Map::class.java.isAssignableFrom(entity.javaClass)) {
			return (entity as Map<*, *>)[key]
		} else {
			throw RuntimeException("Only map is supported, but is [$entity] now.")
		}
	}

	override fun write(entity: Any, value: Any?) {
		if (Map::class.java.isAssignableFrom(entity.javaClass)) {
			@Suppress("UNCHECKED_CAST")
			(entity as MutableMap<Any, Any?>)[key] = value
		} else {
			throw RuntimeException("Only map is supported, but is [$entity] now.")
		}
	}
}

private val id = DynamicFactorDef(
	Factor(factorId = EntityColumns.OBJECT_ID, name = EntityColumns.OBJECT_ID),
	EntityFieldType.ID
)
private val createdAt = DynamicFactorDef(
	Factor(factorId = EntityColumns.CREATED_AT, name = EntityColumns.CREATED_AT),
	EntityFieldType.CREATED_AT
)
private val lastModifiedAt = DynamicFactorDef(
	Factor(factorId = EntityColumns.LAST_MODIFIED_AT, name = EntityColumns.LAST_MODIFIED_AT),
	EntityFieldType.LAST_MODIFIED_AT
)

interface DynamicTopicDef : EntityDef {
	fun getTopic(): Topic
}

abstract class AbstractDynamicTopicDef(private val topic: Topic) :
	AbstractEntityDef(
		topic.name!!,
		listOf(id) + topic.factors.map { factor ->
			DynamicFactorDef(
				factor,
				EntityFieldType.REGULAR
			)
		} + listOf(createdAt, lastModifiedAt)
	), DynamicTopicDef {
	private val fieldsMapByFieldName: Map<String, DynamicFactorDef> =
		fields.map { it as DynamicFactorDef }.map { it.fieldName to it }.toMap()

	override fun getTopic(): Topic {
		return topic
	}

	override fun toPersistObject(entity: Any): PersistObject {
		@Suppress("DuplicatedCode")
		if (!Map::class.java.isAssignableFrom(entity.javaClass)) {
			throw RuntimeException("Only map is supported, but is [$entity] now.")
		}

		@Suppress("UNCHECKED_CAST")
		val map = (entity as Map<String, Any?>).map { (key, value) ->
			val field = findField(key)
			when (val type = field?.factor?.type) {
				FactorType.sequence -> ValueKits.computeToSequence(value) { "Cannot cast value[$value] to sequence." }
				FactorType.number -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to number." }
				FactorType.unsigned -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to unsigned." }

				FactorType.text -> value?.toString()

				FactorType.address -> value?.toString()
				FactorType.continent -> value?.toString()
				FactorType.region -> value?.toString()
				FactorType.country -> value?.toString()
				FactorType.province -> value?.toString()
				FactorType.city -> value?.toString()
				FactorType.district -> value?.toString()
				FactorType.road -> value?.toString()
				FactorType.community -> value?.toString()
				FactorType.floor -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to floor." }
				FactorType.`residence-type` -> value?.toString()
				FactorType.`residential-area` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to floor." }

				FactorType.email -> value?.toString()
				FactorType.phone -> value?.toString()
				FactorType.mobile -> value?.toString()
				FactorType.fax -> value?.toString()

				FactorType.datetime -> ValueKits.computeToDateTime(value) { "Cannot cast value[$value] to datetime." }
				FactorType.`full-datetime` -> ValueKits.computeToDateTime(value) { "Cannot cast value[$value] to full-datetime." }
				FactorType.date -> ValueKits.computeToDate(value) { "Cannot cast value[$value] to date." }
				FactorType.time -> ValueKits.computeToTime(value) { "Cannot cast value[$value] to time." }
				FactorType.year -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to year." }
				FactorType.`half-year` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to half-year." }
				FactorType.quarter -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to quarter." }
				FactorType.month -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to month." }
				FactorType.`half-month` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to half-month." }
				FactorType.`ten-days` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to ten-days." }
				FactorType.`week-of-year` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to week-of-year." }
				FactorType.`week-of-month` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to week-of-month." }
				FactorType.`half-week` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to half-week." }
				FactorType.`day-of-month` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to day-of-month." }
				FactorType.`day-of-week` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to day-of-week." }
				FactorType.`day-kind` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to day-kind." }
				FactorType.hour -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to hour." }
				FactorType.`hour-kind` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to hour-kind." }
				FactorType.minute -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to minute." }
				FactorType.second -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to second." }
				FactorType.millisecond -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to millisecond." }
				FactorType.`am-pm` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to am-pm." }

				FactorType.gender -> value?.toString()
				FactorType.occupation -> value?.toString()
				FactorType.`date-of-birth` -> ValueKits.computeToDate(value) { "Cannot cast value[$value] to date-of-birth." }
				FactorType.age -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to age." }
				FactorType.`id-no` -> value?.toString()
				FactorType.religion -> value?.toString()
				FactorType.nationality -> value?.toString()

				FactorType.`biz-trade` -> value?.toString()
				FactorType.`biz-scale` -> ValueKits.computeToNumeric(value) { "Cannot cast value[$value] to biz-scale." }

				FactorType.boolean -> ValueKits.computeToBoolean(value) { "Cannot cast value[$value] to boolean." }

				FactorType.`enum` -> value?.toString()
				else -> throw RuntimeException("Factor type[$type] is not supported for dynamic topic.")
			}
			toFieldName(field, key) to value
		}.toMap().toMutableMap()
		this.removeEmptyId(map)
		this.handleLastModifiedAt(map)
		return map
	}

	override fun fromPersistObject(po: PersistObject): Any {
		return po.map { (key, value) ->
			val field = fieldsMapByFieldName[key.toLowerCase()]
			(field?.key ?: key) to value
		}.toMap().toMutableMap()
	}

	private fun findField(propertyOrFactorName: String): DynamicFactorDef? {
		return fields.find {
			val field = it as DynamicFactorDef
			val factor = field.factor
			factor.factorId == propertyOrFactorName
					|| factor.name == propertyOrFactorName
					|| it.fieldName == propertyOrFactorName
		} as DynamicFactorDef?
	}

	private fun toFieldName(field: EntityFieldDef?, propertyOrFactorName: String): String {
		return field?.fieldName ?: propertyOrFactorName
	}

	/**
	 * @param propertyOrFactorName might be factor id
	 */
	override fun toFieldName(propertyOrFactorName: String): String {
		return toFieldName(findField(propertyOrFactorName), propertyOrFactorName)
	}

	override fun isMultipleTopicsSupported(): Boolean {
		return false
	}

	/**
	 * @param entityOrTopicName might be topic id
	 */
	override fun isTopicSupported(entityOrTopicName: String): Boolean {
		return entityOrTopicName == key
				|| entityOrTopicName == topic.topicId
				|| entityOrTopicName == collectionName
	}

	override fun toCollectionName(): String {
		return collectionName
	}
}
