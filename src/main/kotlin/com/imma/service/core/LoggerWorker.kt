package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineStage
import com.imma.model.core.PipelineStageUnit
import com.imma.model.core.PipelineStageUnitAction
import com.imma.service.Service
import com.imma.service.Services
import java.io.Closeable

class LoggerWorker(private val instanceId: String, services: Services) : Service(services), Closeable {
    private val logs: MutableList<RunLog> = mutableListOf()

    internal fun append(block: RunLog.() -> Unit) {
        val log = RunLog(
            logId = services.persist().nextSnowflakeId().toString(),
            instanceId = instanceId
        ).apply(block)
        logs.add(log)
    }

    private fun asMonitorLogs(): MonitorLogs {
        TODO("Convert in-memory logs to monitor logs.")
    }

    fun output() {
        // TODO should be autonomous transaction
        services.persist().insertOne(asMonitorLogs(), MonitorLogs::class.java, CollectionNames.RUN_LOG)
    }

    override fun close() {
        services.close()
    }
}

abstract class Logger(private val logger: LoggerWorker) {
    abstract fun fillIds(log: RunLog)

    fun log(msg: String, previous: Map<String, Any>, now: Map<String, Any>) {
        logger.append {
            fillIds(this)
            message = msg
            type = PipelineRunType.start
            oldValue = previous
            newValue = now
        }
    }

    fun log(msg: String, runType: PipelineRunType) {
        logger.append {
            fillIds(this)
            message = msg
            type = runType
        }
    }

    fun log(msg: String, runType: PipelineRunType, spent: Double) {
        logger.append {
            fillIds(this)
            message = msg
            type = runType
            completeTime = spent
        }
    }

    fun error(msg: String, t: Throwable, spent: Double) {
        logger.append {
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

class StageLogger(private val pipeline: Pipeline, private val stage: PipelineStage, logger: LoggerWorker) :
    Logger(logger) {
    override fun fillIds(log: RunLog) {
        log.pipelineId = pipeline.pipelineId
        log.stageId = stage.stageId
    }
}

class UnitLogger(
    private val pipeline: Pipeline,
    private val stage: PipelineStage,
    private val unit: PipelineStageUnit,
    logger: LoggerWorker
) : Logger(logger) {
    override fun fillIds(log: RunLog) {
        log.pipelineId = pipeline.pipelineId
        log.stageId = stage.stageId
        log.unitId = unit.unitId
    }
}

class ActionLogger(
    private val pipeline: Pipeline,
    private val stage: PipelineStage,
    private val unit: PipelineStageUnit,
    private val action: PipelineStageUnitAction,
    logger: LoggerWorker
) : Logger(logger) {
    override fun fillIds(log: RunLog) {
        log.pipelineId = pipeline.pipelineId
        log.stageId = stage.stageId
        log.unitId = unit.unitId
        log.actionId = action.actionId
    }
}