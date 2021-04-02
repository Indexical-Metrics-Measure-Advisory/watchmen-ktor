package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.service.Service
import com.imma.service.Services
import java.io.Closeable

class LoggerWorker(private val instanceId: String, services: Services) : Service(services), Closeable {
    private fun output(instanceId: String, block: RunLog.() -> Unit): RunLog {
        val log = RunLog(
            logId = services.persist().nextSnowflakeId().toString(),
            instanceId = instanceId
        ).apply(block)
        output(log)
        return log
    }

    private fun output(log: RunLog) {
        // TODO should be autonomous transaction
        services.persist().insertOne(log, RunLog::class.java, CollectionNames.RUN_LOG)
    }

    fun log(msg: String, previous: Map<String, Any>, now: Map<String, Any>) {
        output(instanceId) {
            message = msg
            type = PipelineRunType.start
            oldValue = previous
            newValue = now
        }
    }

    fun log(msg: String, runType: PipelineRunType) {
        output(instanceId) {
            message = msg
            type = runType
        }
    }

    fun log(msg: String, runType: PipelineRunType, spent: Double) {
        output(instanceId) {
            message = msg
            type = runType
            completeTime = spent
        }
    }

    fun error(msg: String, t: Throwable, spent: Double) {
        output(instanceId) {
            error = "$msg\nCaused by ${t.stackTraceToString()}."
            type = PipelineRunType.fail
            status = PipelineRunStatus.error
            completeTime = spent
        }
    }

    override fun close() {
        services.close()
    }
}