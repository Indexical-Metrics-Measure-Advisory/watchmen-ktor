package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.Pipeline
import com.imma.model.determineFakeOrNullId
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import kotlin.contracts.ExperimentalContracts

class PipelineService(application: Application) : TupleService(application) {
    @ExperimentalContracts
    fun savePipeline(pipeline: Pipeline) {
        val fake =
            determineFakeOrNullId({ pipeline.pipelineId }, true, { pipeline.pipelineId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(pipeline)
        } else {
            updateTuple(pipeline)
        }
    }

    fun renamePipeline(pipelineId: String, name: String?) {
        writeIntoMongo {
            it.updateFirst(
                Query.query(Criteria.where("pipelineId").`is`(pipelineId)),
                Update().apply {
                    set("name", name)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                Pipeline::class.java,
                CollectionNames.PIPELINE
            )
        }
    }

    fun togglePipelineEnablement(pipelineId: String, enablement: Boolean) {
        writeIntoMongo {
            it.updateFirst(
                Query.query(Criteria.where("pipelineId").`is`(pipelineId)),
                Update().apply {
                    set("enabled", enablement)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                Pipeline::class.java,
                CollectionNames.PIPELINE
            )
        }
    }

    fun findAllPipelines(): List<Pipeline> {
        val query: Query = Query.query(Criteria.where("name").all())
        return findListFromMongo(Pipeline::class.java, CollectionNames.PIPELINE, query)
    }
}