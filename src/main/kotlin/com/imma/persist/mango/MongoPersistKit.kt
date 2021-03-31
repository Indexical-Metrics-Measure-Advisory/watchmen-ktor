package com.imma.persist.mango

import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.AbstractPersistKit
import com.imma.persist.core.*
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import com.imma.utils.findPageData
import com.imma.utils.toDataPage
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

/**
 * thread unsafe
 */
class MongoPersistKit() : AbstractPersistKit() {
    private val mongoClient: MongoClient by lazy {
        createMongoClient()
    }
    private val mongoTemplate: MongoTemplate by lazy {
        createMongoTemplate()
    }

    private fun createMongoClient(): MongoClient {
        val host = Envs.string(EnvConstants.MONGO_HOST)
        val port = Envs.string(EnvConstants.MONGO_PORT)

        return MongoClients.create("mongodb://$host:$port")
    }

    private fun createMongoTemplate(): MongoTemplate {
        val name = Envs.string(EnvConstants.MONGO_NAME)

        return MongoTemplate(SimpleMongoClientDatabaseFactory(mongoClient, name))
    }

    override fun <T> insertOne(one: T, entityClass: Class<T>, entityName: String): T {
        return mongoTemplate.insert(one)
    }

    override fun <T> insertAll(list: List<T>, entityClass: Class<T>, entityName: String): List<T> {
        mongoTemplate.insert(list, entityName)
        return list
    }

    override fun <T> updateOne(one: T, entityClass: Class<T>, entityName: String): T {
        return mongoTemplate.save(one)
    }

    override fun <T> updateOne(where: Where, updates: Updates, entityClass: Class<T>, entityName: String): T? {
        mongoTemplate.updateFirst(buildQuery(where), buildUpdate(updates), entityClass, entityName)

        // TODO retrieve changed data, both old and new values
        return null
    }

    override fun <T> upsert(where: Where, updates: Updates, entityClass: Class<T>, entityName: String): Changed? {
        mongoTemplate.upsert(buildQuery(where), buildUpdate(updates), entityClass, entityName)

        // TODO retrieve changed data, both old and new values
        return null
    }

    override fun <T> update(where: Where, updates: Updates, entityClass: Class<T>, entityName: String): List<Changed> {
        mongoTemplate.updateMulti(buildQuery(where), buildUpdate(updates), entityClass, entityName)

        // TODO retrieve changed data, both old and new values
        return listOf()
    }

    override fun <T> delete(where: Where, entityClass: Class<T>, entityName: String): List<T> {
        mongoTemplate.remove(buildQuery(where), entityClass, entityName)

        // TODO retrieve deleted data
        return listOf()
    }

    override fun <T> deleteAll(entityClass: Class<T>, entityName: String): List<T> {
        mongoTemplate.remove(Query(), entityName)

        // TODO retrieve deleted data
        return listOf()
    }

    override fun <T> findById(id: String, entityClass: Class<T>, entityName: String): T? {
        return mongoTemplate.findById(id, entityClass, entityName)
    }

    override fun <T> findOne(where: Where, entityClass: Class<T>, entityName: String): T? {
        return mongoTemplate.findOne(buildQuery(where), entityClass, entityName)
    }

    override fun <T> exists(where: Where, entityClass: Class<T>, entityName: String): Boolean {
        return mongoTemplate.exists(buildQuery(where), entityClass, entityName)
    }

    override fun <T> listAll(entityClass: Class<T>, entityName: String): List<T> {
        return mongoTemplate.findAll(entityClass, entityName)
    }

    override fun <T> listAll(select: Select, entityClass: Class<T>, entityName: String): List<T> {
        val query = Query()
        query.fields().include(*select.parts.toTypedArray())
        return mongoTemplate.find(query, entityClass, entityName)
    }

    override fun <T> list(where: Where, entityClass: Class<T>, entityName: String): List<T> {
        return mongoTemplate.find(buildQuery(where), entityClass, entityName)
    }

    override fun <T> list(select: Select, where: Where, entityClass: Class<T>, entityName: String): List<T> {
        val query = buildQuery(where)
        query.fields().include(*select.parts.toTypedArray())
        return mongoTemplate.find(query, entityClass, entityName)
    }

    override fun <T> page(pageable: Pageable, entityClass: Class<T>, entityName: String): DataPage<T> {
        // page in PageRequest is zero-based
        val pageRequest: PageRequest = PageRequest.of(pageable.pageNumber - 1, pageable.pageSize)
        val query = Query().with(pageRequest)
        val count = mongoTemplate.count(query, entityClass, entityName)
        val items: List<T> = findPageData(count) { mongoTemplate.find(query, entityClass, entityName) }
        return toDataPage(items, count, pageable)
    }

    override fun <T> page(
        where: Where,
        pageable: Pageable,
        entityClass: Class<T>,
        entityName: String
    ): DataPage<T> {
        // page in PageRequest is zero-based
        val pageRequest: PageRequest = PageRequest.of(pageable.pageNumber - 1, pageable.pageSize)
        val query = buildQuery(where).with(pageRequest)
        val count = mongoTemplate.count(query, entityClass, entityName)
        val items: List<T> = findPageData(count) { mongoTemplate.find(query, entityClass, entityName) }
        return toDataPage(items, count, pageable)
    }

    override fun close() {
        mongoClient.close()
    }

    private fun buildQuery(where: Where): Query {
        val criteria = when (where) {
            is And -> buildAnd(where)
            is Or -> buildOr(where)
            else -> throw RuntimeException("Unsupported where[${where}].")
        }
        return Query(criteria)
    }

    private fun buildAnd(and: And): Criteria {
        val criteria = and.parts.map { exp ->
            when (exp) {
                is And -> buildAnd(exp)
                is Or -> buildOr(exp)
                is ColumnExpression -> buildColumnExpression(exp)
                else -> throw RuntimeException("Unsupported criteria expression[$exp].")
            }
        }
        return Criteria().andOperator(*criteria.toTypedArray())
    }

    private fun buildOr(or: Or): Criteria {
        val criteria = or.parts.map { exp ->
            when (exp) {
                is And -> buildAnd(exp)
                is Or -> buildOr(exp)
                is ColumnExpression -> buildColumnExpression(exp)
                else -> throw RuntimeException("Unsupported criteria expression[$exp].")
            }
        }
        return Criteria().orOperator(*criteria.toTypedArray())
    }

    private fun buildColumnExpression(exp: ColumnExpression): Criteria {
        return when (exp.operator) {
            ColumnExpressionOperator.EQUALS -> Criteria.where(exp.column.name).`is`(exp.value)
            ColumnExpressionOperator.IN -> Criteria.where(exp.column.name).`in`(exp.value)
            ColumnExpressionOperator.INCLUDE -> Criteria.where(exp.column.name).`is`(exp.value)
            ColumnExpressionOperator.REGEXP -> Criteria.where(exp.column.name).regex(exp.value as String, "i")
            ColumnExpressionOperator.NOT_SET -> throw RuntimeException("Unsupported criteria expression operator[${exp.operator}].")
            else -> throw RuntimeException("Unsupported criteria expression operator[${exp.operator}].")
        }
    }

    private fun buildUpdate(updates: Updates): Update {
        return Update().apply {
            val update = this
            updates.parts.forEach {
                @Suppress("REDUNDANT_ELSE_IN_WHEN")
                when (it.type) {
                    ColumnUpdateType.SET -> update.set(it.column.name, it.value)
                    ColumnUpdateType.PULL -> update.pull(it.column.name, it.value)
                    ColumnUpdateType.PUSH -> update.push(it.column.name, it.value)
                    else -> throw RuntimeException("Unsupported column update[type=${it.type}].")
                }
            }
        }
    }
}