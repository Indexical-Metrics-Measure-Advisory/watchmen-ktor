package com.imma.model

object CollectionNames {
	const val USER: String = "users"
	const val USER_CREDENTIAL: String = "user_credentials"
	const val USER_GROUP: String = "user_groups"

	const val SPACE: String = "spaces"

	const val CONNECTED_SPACE: String = "connected_spaces"
	const val CONNECTED_SPACE_GRAPHICS: String = "connected_space_graphics"
	const val SUBJECT: String = "subjects"
	const val REPORT: String = "reports"
	const val DASHBOARD: String = "dashboards"
	const val FAVORITE: String = "favorites"
	const val LAST_SNAPSHOT: String = "last_snapshots"

	const val ENUM: String = "enums"
	const val TOPIC: String = "topics"
	const val PIPELINE: String = "pipelines"
	const val PIPELINE_GRAPHICS: String = "pipeline_graphics"

	const val RUNTIME_PIPELINE_LOG: String = "runtime_pipeline_logs"
}

object EntityColumns {
	const val OBJECT_ID = "_id"
	const val CREATED_AT = "_create_time"
	const val LAST_MODIFIED_AT = "_last_modify_time"

	const val AGGREGATE_ASSIST = "_aggregate_assist"
	const val EMPTY_AGGREGATE_ASSIST = "{}"
	const val AVG_COUNT = "avg_count"
}

object ConstantPredefines {
	const val NEXT_SEQ = "&nextSeq"
	const val COUNT = "&count"
	const val LENGTH = "&length"
	const val SUM = "&sum"
	const val FROM_PREVIOUS_TRIGGER_DATA = "&old"
}