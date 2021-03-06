package com.imma.service

import com.imma.persist.mango.findPageFromMongo
import com.imma.persist.mango.writeIntoMongo
import com.imma.persist.snowflake.nextSnowflakeId
import com.imma.rest.DataPage
import com.imma.rest.Pageable
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

    fun <T> findPageFromMongo(
        entityClass: Class<T>,
        collectionName: String,
        query: Query,
        pageable: Pageable
    ): DataPage<T> {
        return application.findPageFromMongo(entityClass, collectionName, query, pageable)
    }
}