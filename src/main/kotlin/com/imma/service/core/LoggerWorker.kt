package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.service.Service
import com.imma.service.Services

class LoggerWorker(services: Services) : Service(services) {
    fun output(instanceId: String, block: RunLog.() -> Unit): RunLog {
        val log = RunLog(
            logId = services.persist().nextSnowflakeId().toString(),
            instanceId = instanceId
        ).apply(block)
        output(log)
        return log
    }

    fun output(log: RunLog) {
        // TODO should be autonomous transaction
        services.persist().insertOne(log, RunLog::class.java, CollectionNames.RUN_LOG)
    }
}