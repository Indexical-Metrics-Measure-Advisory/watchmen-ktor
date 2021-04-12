package com.imma.persist.mango

import com.imma.utils.getCurrentDateTime
import org.bson.Document
import org.jetbrains.kotlin.utils.doNothing

enum class EntityFieldType {
    ID, CREATED_AT, LAST_MODIFIED_AT, REGULAR
}

abstract class EntityFieldDef(val name: String, val type: EntityFieldType) {
    abstract fun read(entity: Any): Any?
    abstract fun write(entity: Any, value: Any?)
}

abstract class EntityDef(val name: String, val fields: List<EntityFieldDef>) {
    init {
        fields.filter { it.type == EntityFieldType.ID }.size.let {
            when {
                it == 0 -> throw RuntimeException("Id field not defined.")
                it > 1 -> throw RuntimeException("One and only one id field for an entity, current $it found.")
                else -> doNothing()
            }
        }
        fields.filter { it.type == EntityFieldType.CREATED_AT }.size.let {
            if (it > 1) {
                throw RuntimeException("One and only one created-at field for an entity, current $it found.")
            }
        }
        fields.filter { it.type == EntityFieldType.LAST_MODIFIED_AT }.size.let {
            if (it > 1) {
                throw RuntimeException("One and only one last-modified-at field for an entity, current $it found.")
            }
        }
    }

    val id: EntityFieldDef = fields.find { def -> def.type == EntityFieldType.ID }
        ?: throw RuntimeException("Id field not defined.")
    val createdAt: EntityFieldDef? = fields.find { def -> def.type == EntityFieldType.CREATED_AT }
    val lastModifiedAt: EntityFieldDef? = fields.find { def -> def.type == EntityFieldType.LAST_MODIFIED_AT }

    protected fun removeEmptyId(map: MutableMap<String, Any?>) {
        id.let { id ->
            val value = map[id.name]
            if (value == null || (value is String && value.isBlank())) {
                map -= id.name
            }
        }
    }

    /**
     * handle create time
     */
    protected fun handleCreatedAt(map: MutableMap<String, Any?>) {
        val now = getCurrentDateTime()
        createdAt?.let { created ->
            val value = map[created.name]
            if (value == null) {
                map[created.name] = now
            }
        }
    }

    /**
     * handle last modify time
     */
    protected fun handleLastModifiedAt(map: MutableMap<String, Any?>) {
        val now = getCurrentDateTime()
        lastModifiedAt?.let { lastModified ->
            map[lastModified.name] = now
        }
    }

    /**
     * convert given entity to document
     */
    abstract fun toDocument(entity: Any): Document

    fun toDocument(entity: Any, generateId: () -> Any): Document {
        this.tryToFillId(entity, generateId)
        val doc = this.toDocument(entity)
        this.handleCreatedAt(doc)
        return doc
    }

    /**
     * @return true when id was filled
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun tryToFillId(entity: Any, generateId: () -> Any): Boolean {
        val value = id.read(entity)
        return when {
            value == null || (value is String && value.isBlank()) -> {
                id.write(entity, generateId())
                true
            }
            else -> false
        }
    }

    /**
     * generate id filter
     */
    fun generateIdFilter(entity: Any): Document {
        val value = id.read(entity)
        return when {
            value == null -> throw RuntimeException("Cannot generate id filter when value of id is null.")
            value is String && value.isBlank() -> throw RuntimeException("Cannot generate id filter when value of id is blank.")
            else -> Document(id.name, value)
        }
    }

    /**
     * use field name from definition, or return itself when not matched
     *
     * @param propertyOrFactorName property name of bean, or factor name of topic (static and dynamic)
     * @return factor name of topic
     */
    abstract fun toFactorName(propertyOrFactorName: String): String

    abstract fun isMultipleTopicsSupported(): Boolean

    /**
     * check given entity or topic name is supported or not
     */
    abstract fun isTopicSupported(entityOrTopicName: String): Boolean
}
