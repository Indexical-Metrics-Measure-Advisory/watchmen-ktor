package com.imma.persist

import com.imma.persist.snowflake.SnowflakeIdGenerator
import com.imma.persist.snowflake.SnowflakeIdWorker
import com.imma.utils.EnvConstants
import io.ktor.application.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

var snowflakeWorker: SnowflakeIdWorker? = null

abstract class AbstractPersistKit(val application: Application) : PersistKit {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(this::class.java)
    }

    protected fun logger(): Logger {
        return logger
    }

    override fun nextSnowflakeId(): Long {
        if (snowflakeWorker == null) {
            logger.warn("Snowflake worker: [{}]", snowflakeWorker)
            synchronized(this) {
                if (snowflakeWorker == null) {
                    val env = application.environment
                    val workerId = env.config.property(EnvConstants.SNOWFLAKE_WORKER).getString().toLong()
                    val dataCenterId = env.config.property(EnvConstants.SNOWFLAKE_DATA_CENTER).getString().toLong()
                    snowflakeWorker = SnowflakeIdGenerator().createWorker(workerId, dataCenterId, true)
                }
            }
        }
        return snowflakeWorker!!.nextId()
    }
}