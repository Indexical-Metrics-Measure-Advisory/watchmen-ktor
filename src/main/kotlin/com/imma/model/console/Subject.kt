package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.model.core.compute.ParameterDelegate
import com.imma.model.core.compute.ParameterJointDelegate
import com.imma.persist.annotation.*
import java.util.*

data class SubjectDataSetColumn(
	var columnId: String = "",
	var parameter: ParameterDelegate = mutableMapOf(),
	var alias: String = ""
)

@Suppress("EnumEntryName")
enum class TopicJoinType(val type: String) {
	left("left"),
	right("right"),
	`inner`("inner");
}

data class SubjectDataSetJoin(
	var topicId: String = "",
	var factorId: String = "",
	var secondaryTopicId: String = "",
	var secondaryFactorId: String = "",
	var type: TopicJoinType = TopicJoinType.`inner`,
)

data class SubjectDataSet(
	var filters: ParameterJointDelegate = mutableMapOf(),
	var columns: List<SubjectDataSetColumn> = mutableListOf(),
	var joins: List<SubjectDataSetJoin> = mutableListOf()
)

@Entity(CollectionNames.SUBJECT)
data class Subject(
	@Id
	var subjectId: String? = null,
	@Field("name")
	var name: String? = null,
	@Field("connect_id")
	var connectId: String? = null,
	@Field("user_id")
	var userId: String? = null,
	@Field("auto_refresh_interval")
	var autoRefreshInterval: Boolean? = null,
	@Field("dataset")
	var dataset: SubjectDataSet = SubjectDataSet(),
	@Field("last_visit_time")
	var lastVisitTime: String? = null,
	@CreatedAt
	override var createTime: Date? = null,
	@LastModifiedAt
	override var lastModifyTime: Date? = null,
) : Tuple() {
	@Transient
	var reports: MutableList<Report> = mutableListOf()
}