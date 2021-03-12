package com.imma.model.core

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

data class BlockCoordinate(
    var x: Float = 0f,
    var y: Float = 0f
)

data class BlockFrame(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f
)

data class BlockName(
    var x: Float = 0f,
    var y: Float = 0f
)

data class PipelineBlockGraphicsRect(
    var coordinate: BlockCoordinate = BlockCoordinate(),
    var frame: BlockFrame = BlockFrame(),
    var name: BlockName = BlockName()
)

data class TopicGraphics(
    var topicId: String = "",
    var rect: PipelineBlockGraphicsRect = PipelineBlockGraphicsRect()
)

@Document(collection = CollectionNames.PIPELINE_GRAPHICS)
data class PipelineGraphics(
    @Id
    var userId: String? = null,
    @Field("topics")
    var topics: List<TopicGraphics> = mutableListOf()
)