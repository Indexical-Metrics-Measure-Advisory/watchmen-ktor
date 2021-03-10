package com.imma.persist.snowflake

import com.imma.utils.EnvConstants
import io.ktor.application.*

var snowflakeWorker: SnowflakeIdWorker? = null

fun Application.nextSnowflakeId(): Long {
    log.warn("Snowflake worker: [{}]", snowflakeWorker)
    if (snowflakeWorker == null) {
        synchronized(this) {
            if (snowflakeWorker == null) {
                val workerId = environment.config.property(EnvConstants.SNOWFLAKE_WORKER).getString().toLong()
                val dataCenterId = environment.config.property(EnvConstants.SNOWFLAKE_DATA_CENTER).getString().toLong()
                snowflakeWorker = SnowflakeIdGenerator().createWorker(workerId, dataCenterId, true)
            }
        }
    }
    return snowflakeWorker!!.nextId()
}