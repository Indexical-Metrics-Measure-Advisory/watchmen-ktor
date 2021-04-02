package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.service.Service
import com.imma.service.Services
import java.io.Closeable

class LoggerWorker(private val instanceId: String, services: Services) : Service(services), Closeable {
    internal fun output(block: RunLog.() -> Unit): RunLog {
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

    override fun close() {
        services.close()
    }
}

abstract class Logger(private val logger: LoggerWorker) {
    abstract fun fillIds(log: RunLog)

    fun log(msg: String, previous: Map<String, Any>, now: Map<String, Any>) {
        logger.output {
            fillIds(this)
            message = msg
            type = PipelineRunType.start
            oldValue = previous
            newValue = now
        }
    }

    fun log(msg: String, runType: PipelineRunType) {
        logger.output {
            fillIds(this)
            message = msg
            type = runType
        }
    }

    fun log(msg: String, runType: PipelineRunType, spent: Double) {
        logger.output {
            fillIds(this)
            message = msg
            type = runType
            completeTime = spent
        }
    }

    fun error(msg: String, t: Throwable, spent: Double) {
        logger.output {
            fillIds(this)
            error = "$msg\nCaused by ${t.stackTraceToString()}."
            type = PipelineRunType.fail
            status = PipelineRunStatus.error
            completeTime = spent
        }
    }
}

class PipelineLogger(private val pipeline: Pipeline, logger: LoggerWorker) : Logger(logger) {
    override fun fillIds(log: RunLog) {
        log.pipelineId = pipeline.pipelineId
    }
}

class StageLogger(private val pipeline: Pipeline, private val stage: PipelineStage, logger: LoggerWorker) : Logger(logger) {
    override fun fillIds(log: RunLog) {
        log.pipelineId = pipeline.pipelineId
        log.stageId = stage.stageId
    }
}