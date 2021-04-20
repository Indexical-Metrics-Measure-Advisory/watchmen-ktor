package com.imma.persist.mango

import com.imma.model.core.Factor
import com.imma.model.core.Topic
import org.bson.Document

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

private val id = DynamicFactorDef(Factor(factorId = "_id", name = "_id"), EntityFieldType.ID)
private val createdAt = DynamicFactorDef(
    Factor(factorId = "_create_time", name = "_create_time"),
    EntityFieldType.CREATED_AT
)
private val lastModifiedAt = DynamicFactorDef(
    Factor(factorId = "_last_modify_time", name = "_last_modify_time"),
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
    private val fieldsMapByFieldName: Map<String, DynamicFactorDef> =
        fields.map { it as DynamicFactorDef }.map { it.fieldName to it }.toMap()

    override fun toDocument(entity: Any): Document {
        @Suppress("DuplicatedCode")
        if (!Map::class.java.isAssignableFrom(entity.javaClass)) {
            throw RuntimeException("Only map is supported, but is [$entity] now.")
        }

        @Suppress("UNCHECKED_CAST")
        val map = (entity as Map<String, Any?>).map { (key, value) ->
            toFieldName(key) to value
        }.toMap().toMutableMap()
        this.removeEmptyId(map)
        this.handleLastModifiedAt(map)
        return Document(map)
    }

    override fun fromDocument(doc: Document): Any {
        return doc.map { (key, value) ->
            val field = fieldsMapByFieldName[key.toLowerCase()]
            (field?.key ?: key) to value
        }.toMap().toMutableMap()
    }

    /**
     * @param propertyOrFactorName might be factor id
     */
    override fun toFieldName(propertyOrFactorName: String): String {
        val field = fields.find {
            val field = it as DynamicFactorDef
            val factor = field.factor
            factor.factorId == propertyOrFactorName
                    || factor.name == propertyOrFactorName
                    || it.fieldName == propertyOrFactorName
        }

        return field?.fieldName ?: propertyOrFactorName
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
