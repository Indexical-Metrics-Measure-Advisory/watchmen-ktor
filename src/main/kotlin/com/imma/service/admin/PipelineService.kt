package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.core.Pipeline
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
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
			where { factor("pipelineId") eq { value(pipelineId) } },
			update { set("name") to name },
			Pipeline::class.java, CollectionNames.PIPELINE
		)
	}

	fun togglePipelineEnablement(pipelineId: String, enablement: Boolean) {
		persist().updateOne(
			where { factor("pipelineId") eq { value(pipelineId) } },
			update { set("enabled") to enablement },
			Pipeline::class.java, CollectionNames.PIPELINE
		)
	}

	fun listPipelines(topicId: String): List<Pipeline> {
		return persist().list(where {
			factor("topicId") eq { value(topicId) }
		}, Pipeline::class.java, CollectionNames.PIPELINE)
	}

	fun findAllPipelines(): List<Pipeline> {
		return persist().listAll(Pipeline::class.java, CollectionNames.PIPELINE)
	}
}