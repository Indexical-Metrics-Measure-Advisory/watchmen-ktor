package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.core.Pipeline
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import kotlin.contracts.ExperimentalContracts

class PipelineService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun savePipeline(pipeline: Pipeline) {
        val fake =
            determineFakeOrNullId({ pipeline.pipelineId }, true, { pipeline.pipelineId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(pipeline, Pipeline::class.java, CollectionNames.PIPELINE)
        } else {
            updateTuple(pipeline, Pipeline::class.java, CollectionNames.PIPELINE)
        }
    }

    fun renamePipeline(pipelineId: String, name: String?) {
        persist().updateOne(
            where {
                column("pipelineId") eq pipelineId
            },
            update {
                set("name") to name
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            Pipeline::class.java, CollectionNames.PIPELINE
        )
    }

    fun togglePipelineEnablement(pipelineId: String, enablement: Boolean) {
        persist().updateOne(
            where {
                column("pipelineId") eq pipelineId
            },
            update {
                set("enabled") to enablement
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            Pipeline::class.java, CollectionNames.PIPELINE
        )
    }

    fun findAllPipelines(): List<Pipeline> {
        return persist().listAll(Pipeline::class.java, CollectionNames.PIPELINE)
    }
}