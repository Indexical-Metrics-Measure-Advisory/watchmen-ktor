package com.imma.persist

import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.core.Select
import com.imma.persist.core.Updates
import com.imma.persist.core.Where
import java.io.Closeable

/**
 * persistent kit
 */
interface PersistKit : Closeable {
    fun nextSnowflakeId(): Long

    fun <T : Any> insertOne(one: T, entityClass: Class<*>, entityName: String): T

    fun <T : Any> insertAll(list: List<T>, entityClass: Class<*>, entityName: String): List<T>

    fun <T : Any> updateOne(one: T, entityClass: Class<*>, entityName: String): T

    fun updateOne(where: Where, updates: Updates, entityClass: Class<*>, entityName: String)

    fun <T : Any> upsertOne(one: T, entityClass: Class<*>, entityName: String): T

    fun update(where: Where, updates: Updates, entityClass: Class<*>, entityName: String)

    fun deleteById(id: String, entityClass: Class<*>, entityName: String)

    fun delete(where: Where, entityClass: Class<*>, entityName: String)

    fun deleteAll(entityClass: Class<*>, entityName: String)

    fun <T> findById(id: String, entityClass: Class<*>, entityName: String): T?

    fun <T> findOne(where: Where, entityClass: Class<*>, entityName: String): T?

    fun exists(where: Where, entityClass: Class<*>, entityName: String): Boolean

    fun <T> listAll(entityClass: Class<*>, entityName: String): List<T>

    fun <T> listAll(select: Select, entityClass: Class<*>, entityName: String): List<T>

    fun <T> list(where: Where, entityClass: Class<*>, entityName: String): List<T>

    fun <T> list(select: Select, where: Where, entityClass: Class<*>, entityName: String): List<T>

    fun <T> page(pageable: Pageable, entityClass: Class<*>, entityName: String): DataPage<T>

    fun <T> page(where: Where, pageable: Pageable, entityClass: Class<*>, entityName: String): DataPage<T>

    /**
     * check entity existing, not data
     */
    fun entityExists(entityClass: Class<*>, entityName: String): Boolean

    /**
     * create entity, not data
     */
    fun createEntity(entityClass: Class<*>, entityName: String)
}
