package com.imma.persist.mango

import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.AbstractPersistKit
import com.imma.persist.core.Changed
import com.imma.persist.core.Changes
import com.imma.persist.core.Select
import com.imma.persist.core.Where
import com.imma.utils.EnvConstants
import com.imma.utils.findPageData
import com.imma.utils.toDataPage
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.ktor.application.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

/**
 * thread unsafe
 */
class MongoPersistKit(application: Application) : AbstractPersistKit(application) {
    private val mongoClient: MongoClient by lazy {
        createMongoClient()
    }
    private val mongoTemplate: MongoTemplate by lazy {
        createMongoTemplate()
    }

    private fun createMongoClient(): MongoClient {
        val env = application.environment
        val host = env.config.property(EnvConstants.MONGO_HOST).getString()
        val port = env.config.property(EnvConstants.MONGO_PORT).getString()

        return MongoClients.create("mongodb://$host:$port")
    }

    private fun createMongoTemplate(): MongoTemplate {
        val env = application.environment
        val name = env.config.property(EnvConstants.MONGO_NAME).getString()

        return MongoTemplate(SimpleMongoClientDatabaseFactory(mongoClient, name))
    }

    override fun <T> insertOne(one: T): T {
        return mongoTemplate.insert(one)
    }

    override fun <T> updateOne(one: T): T {
        return mongoTemplate.save(one)
    }

    override fun <T> upsert(where: Where, changes: Changes, entityClass: Class<T>, entityName: String): Changed? {
        mongoTemplate.upsert(buildQuery(where), buildUpdate(changes), entityClass, entityName)

        // TODO retrieve changed data, both old and new values
        return null
    }

    override fun <T> update(where: Where, changes: Changes, entityClass: Class<T>, entityName: String): List<Changed> {
        mongoTemplate.updateMulti(buildQuery(where), buildUpdate(changes), entityClass, entityName)

        // TODO retrieve changed data, both old and new values
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
        // TODO transform where to query
        return Query.query(Criteria.where("mock"))
    }

    private fun buildUpdate(changes: Changes): Update {
        // TODO transform changes to update
        return Update.update("mock", "value")
    }
}