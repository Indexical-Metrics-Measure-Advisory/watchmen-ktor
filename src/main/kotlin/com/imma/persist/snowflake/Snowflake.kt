package com.imma.persist.snowflake

import io.ktor.application.*

var snowflakeWorker: SnowflakeIdWorker? = null

fun Application.nextSnowflakeId(): Long {
    log.warn("Snowflake worker: [{}]", snowflakeWorker)
    if (snowflakeWorker == null) {
        synchronized(this) {
            if (snowflakeWorker == null) {
                val workerId = environment.config.property("ktor.snowflake.worker").getString().toLong()
                val dataCenterId = environment.config.property("ktor.snowflake.dataCenter").getString().toLong()
                snowflakeWorker = SnowflakeIdGenerator().createWorker(workerId, dataCenterId, true)
            }
        }
    }
    return snowflakeWorker!!.nextId()
}