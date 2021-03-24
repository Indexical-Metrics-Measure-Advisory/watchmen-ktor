package com.imma.service

import com.imma.persist.PersistKit
import org.springframework.data.mongodb.core.MongoTemplate

/**
 * thread unsafe
 */
open class Service(protected val services: Services) {
    fun nextSnowflakeId(): Long {
        return services.persist().nextSnowflakeId()
    }

    fun persist(): PersistKit {
        return services.persist()
    }
}