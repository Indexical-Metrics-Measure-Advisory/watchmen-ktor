package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

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

data class ConnectedSpaceBlockGraphicsRect(
	var coordinate: BlockCoordinate = BlockCoordinate(),
	var frame: BlockFrame = BlockFrame(),
	var name: BlockName = BlockName()
)

data class TopicGraphics(
	var topicId: String = "",
	var rect: ConnectedSpaceBlockGraphicsRect = ConnectedSpaceBlockGraphicsRect()
)

data class SubjectGraphics(
	var subjectId: String = "",
	var rect: ConnectedSpaceBlockGraphicsRect = ConnectedSpaceBlockGraphicsRect()
)

data class ReportGraphics(
	var reportId: String = "",
	var rect: ConnectedSpaceBlockGraphicsRect = ConnectedSpaceBlockGraphicsRect()
)

@Entity(CollectionNames.CONNECTED_SPACE_GRAPHICS)
data class ConnectedSpaceGraphics(
	@Id
	var connectId: String? = null,
	@Field("user_id")
	var userId: String? = null,
	@Field("topics")
	var topics: List<TopicGraphics> = mutableListOf(),
	@Field("subjects")
	var subjects: List<SubjectGraphics> = mutableListOf(),
	@Field("reports")
	var reports: List<ReportGraphics> = mutableListOf(),
)