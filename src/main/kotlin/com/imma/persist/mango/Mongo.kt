package com.imma.persist.mango

import com.imma.rest.DataPage
import com.imma.rest.Pageable
import com.imma.utils.EnvConstants
import com.imma.utils.findPageData
import com.imma.utils.toDataPage
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.ktor.application.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.query.Query

private fun Application.createMongoClient(): MongoClient {
    val host = environment.config.property(EnvConstants.MONGO_HOST).getString()
    val port = environment.config.property(EnvConstants.MONGO_PORT).getString()

    return MongoClients.create("mongodb://$host:$port")
}

private fun Application.createMongoTemplate(client: MongoClient): MongoTemplate {
    val name = environment.config.property(EnvConstants.MONGO_NAME).getString()

    return MongoTemplate(SimpleMongoClientDatabaseFactory(client, name))
}

fun Application.writeIntoMongo(action: (template: MongoTemplate) -> Unit) {
    val mongoClient = createMongoClient()
    val mongoTemplate = createMongoTemplate(mongoClient)
    action(mongoTemplate)
    mongoClient.close()
}

fun <T> Application.findFromMongo(action: (template: MongoTemplate) -> T): T? {
    val mongoClient = createMongoClient()
    val mongoTemplate = createMongoTemplate(mongoClient)
    val found = action(mongoTemplate)
    mongoClient.close()
    return found
}

fun <T> Application.getFromMongo(action: (template: MongoTemplate) -> T): T {
    val mongoClient = createMongoClient()
    val mongoTemplate = createMongoTemplate(mongoClient)
    val found = action(mongoTemplate)
    mongoClient.close()
    return found
}

fun <T> Application.findListFromMongo(
    entityClass: Class<T>,
    collectionName: String,
    query: Query,
): List<T> {
    return getFromMongo { it.find(query, entityClass, collectionName) }
}

fun <T> Application.findPageFromMongo(
    entityClass: Class<T>,
    collectionName: String,
    query: Query,
    pageable: Pageable
): DataPage<T> {
    return getFromMongo {
        // page in PageRequest is zero-based
        val pageRequest: PageRequest = PageRequest.of(pageable.pageNumber - 1, pageable.pageSize)
        query.with(pageRequest)
        val count = it.count(query, entityClass, collectionName)
        val items: List<T> = findPageData(count) { it.find(query, entityClass, collectionName) }
        toDataPage(items, count, pageable)
    }
}