package com.imma.service

import com.imma.persist.mango.*
import com.imma.persist.mango.findFromMongo
import com.imma.persist.snowflake.nextSnowflakeId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import io.ktor.application.*
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

open class Service(val application: Application) {
    fun nextSnowflakeId(): Long {
        return application.nextSnowflakeId()
    }

    fun writeIntoMongo(action: (template: MongoTemplate) -> Unit) {
        return application.writeIntoMongo(action)
    }

    fun <T> findFromMongo(action: (template: MongoTemplate) -> T): T? {
        return application.findFromMongo(action)
    }

    fun <T> getFromMongo(action: (template: MongoTemplate) -> T): T {
        return application.getFromMongo(action)
    }

    fun <T> findListFromMongo(
        entityClass: Class<T>,
        collectionName: String,
        query: Query,
    ): List<T> {
        return application.findListFromMongo(entityClass, collectionName, query)
    }

    fun <T> findPageFromMongo(
        entityClass: Class<T>,
        collectionName: String,
        query: Query,
        pageable: Pageable
    ): DataPage<T> {
        return application.findPageFromMongo(entityClass, collectionName, query, pageable)
    }
}