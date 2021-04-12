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

    fun <T> insertOne(one: T, entityClass: Class<T>, entityName: String): T

    fun <T> insertAll(list: List<T>, entityClass: Class<T>, entityName: String): List<T>

    fun <T> updateOne(one: T, entityClass: Class<T>, entityName: String): T

    fun <T> updateOne(where: Where, updates: Updates, entityClass: Class<T>, entityName: String)

    fun <T> upsert(where: Where, updates: Updates, entityClass: Class<T>, entityName: String)

    fun <T> update(where: Where, updates: Updates, entityClass: Class<T>, entityName: String)

    fun <T> delete(where: Where, entityClass: Class<T>, entityName: String)

    fun <T> deleteAll(entityClass: Class<T>, entityName: String)

    fun <T> findById(id: String, entityClass: Class<T>, entityName: String): T?

    fun <T> findOne(where: Where, entityClass: Class<T>, entityName: String): T?

    fun <T> exists(where: Where, entityClass: Class<T>, entityName: String): Boolean

    fun <T> listAll(entityClass: Class<T>, entityName: String): List<T>

    fun <T> listAll(select: Select, entityClass: Class<T>, entityName: String): List<T>

    fun <T> list(where: Where, entityClass: Class<T>, entityName: String): List<T>

    fun <T> list(select: Select, where: Where, entityClass: Class<T>, entityName: String): List<T>

    fun <T> page(pageable: Pageable, entityClass: Class<T>, entityName: String): DataPage<T>

    fun <T> page(where: Where, pageable: Pageable, entityClass: Class<T>, entityName: String): DataPage<T>
}
