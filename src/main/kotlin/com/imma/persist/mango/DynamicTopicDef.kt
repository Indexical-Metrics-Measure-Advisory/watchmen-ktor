package com.imma.persist.mango

import com.imma.model.core.Factor
import com.imma.model.core.Topic
import com.imma.persist.DynamicTopicUtils
import org.bson.Document

class DynamicFactorDef(val factor: Factor, type: EntityFieldType) : EntityFieldDef(factor.name!!, type) {
    val fieldName: String = DynamicTopicUtils.toFieldName(factor.name!!)

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

private val id = DynamicFactorDef(Factor(factorId = "_id", name = "_id"), EntityFieldType.ID)
private val createdAt = DynamicFactorDef(
    Factor(factorId = "create_time", name = "create_time"),
    EntityFieldType.CREATED_AT
)
private val lastModifiedAt = DynamicFactorDef(
    Factor(factorId = "last_modify_time", name = "last_modify_time"),
    EntityFieldType.LAST_MODIFIED_AT
)

class DynamicTopicDef(val topic: Topic) :
    EntityDef(
        topic.name!!,
        listOf(id) + topic.factors.map { factor ->
            DynamicFactorDef(
                factor,
                EntityFieldType.REGULAR
            )
        } + listOf(createdAt, lastModifiedAt)
    ) {
    private val collectionName = DynamicTopicUtils.toCollectionName(key)
    private val fieldsMapByFactorName: Map<String, DynamicFactorDef> =
        fields.map { it.key to (it as DynamicFactorDef) }.toMap()
    private val fieldsMapByFieldName: Map<String, DynamicFactorDef> =
        fields.map { it as DynamicFactorDef }.map { it.fieldName to it }.toMap()

    /**
     * convert key to persist name if definition found, otherwise keep it.
     */
    private fun toPersistDocument(map: Map<String, Any?>): Document {
        return Document().let { doc ->
            map.map { (key, value) ->
                val field = fieldsMapByFactorName[key]
                if (field != null) {
                    // convert name to field name
                    doc.append(field.fieldName, value)
                } else {
                    // not found in definition, just let be
                    doc.append(key, value)
                }
            }
            doc
        }
    }

    override fun toDocument(entity: Any): Document {
        @Suppress("DuplicatedCode")
        if (!Map::class.java.isAssignableFrom(entity.javaClass)) {
            throw RuntimeException("Only map is supported, but is [$entity] now.")
        }

        @Suppress("UNCHECKED_CAST")
        val map = (entity as Map<String, Any?>).toMutableMap()
        this.removeEmptyId(map)
        this.handleLastModifiedAt(map)
        return toPersistDocument(map)
    }

    /**
     * convert key to factor name if definition found, otherwise keep it.
     */
    private fun fromPersistDocument(doc: Document): MutableMap<String, Any?> {
        return doc.map { (key, value) ->
            val field = fieldsMapByFieldName[key.toLowerCase()]
            if (field != null) {
                field.key to value
            } else {
                key to value
            }
        }.toMap().toMutableMap()
    }

    override fun fromDocument(doc: Document): Any {
        return fromPersistDocument(doc)
    }

    /**
     * @param propertyOrFactorName might be factor id
     */
    override fun toFieldName(propertyOrFactorName: String): String {
        val field = fields.find {
            val field = it as DynamicFactorDef
            val factor = field.factor
            factor.factorId == propertyOrFactorName || factor.name == propertyOrFactorName
        }

        return if (field == null) {
            propertyOrFactorName
        } else {
            (field as DynamicFactorDef).fieldName
        }
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

fun createDynamicTopicDef(topic: Topic): DynamicTopicDef {
    return DynamicTopicDef(topic)
}
