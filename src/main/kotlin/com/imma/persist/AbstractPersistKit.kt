package com.imma.persist

import com.imma.persist.snowflake.SnowflakeIdGenerator
import com.imma.persist.snowflake.SnowflakeIdWorker
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

var snowflakeWorker: SnowflakeIdWorker? = null

abstract class AbstractPersistKit : PersistKit {
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
                    val workerId = Envs.long(EnvConstants.SNOWFLAKE_WORKER)
                    val dataCenterId = Envs.long(EnvConstants.SNOWFLAKE_DATA_CENTER)
                    snowflakeWorker = SnowflakeIdGenerator().createWorker(workerId, dataCenterId, true)
                }
            }
        }
        return snowflakeWorker!!.nextId()
    }
}