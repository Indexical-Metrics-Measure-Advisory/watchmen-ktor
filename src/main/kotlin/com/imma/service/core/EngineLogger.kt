package com.imma.service.core

import com.imma.service.Service
import com.imma.service.Services
import com.imma.service.core.log.RunLog
import com.imma.service.core.log.RunStatus
import com.imma.service.core.log.RunType
import java.io.Closeable

class EngineLogger(private val instanceId: String, services: Services) : Service(services), Closeable {
    private val logs: MutableList<RunLog> = mutableListOf()

    internal fun append(block: RunLog.() -> Unit) {
        val log = RunLog(
            logId = services.persist().nextSnowflakeId().toString(),
            instanceId = instanceId
        ).apply(block)
        logs += log
    }

    fun output() {
        // TODO should be autonomous transaction
    }

    override fun close() {
        services.close()
    }
}

abstract class EngineLoggerDelegate(protected val logger: EngineLogger) {
    abstract fun fillIds(log: RunLog)

    fun log(msg: String, runType: RunType) {
        logger.append {
            fillIds(this)
            message = msg
            type = runType
        }
    }

    fun log(map: Map<String, Any?>, runType: RunType) {
        logger.append {
            fillIds(this)
            data = map
            type = runType
        }
    }

    fun start(msg: String) {
        logger.append {
            fillIds(this)
            message = msg
            type = RunType.start
        }
    }

    fun ignore(msg: String) {
        logger.append {
            fillIds(this)
            message = msg
            type = RunType.ignore
            completeTime = 0.toDouble()
        }
    }

    fun success(msg: String, spent: Double) {
        logger.append {
            fillIds(this)
            message = msg
            type = RunType.end
            completeTime = spent
        }
    }

    fun fail(msg: String, t: Throwable, spent: Double) {
        logger.append {
            fillIds(this)
            error = "$msg\nCaused by ${t.stackTraceToString()}."
            type = RunType.fail
            status = RunStatus.error
            completeTime = spent
        }
    }
}
