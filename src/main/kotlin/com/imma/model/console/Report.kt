package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.model.chart.Chart
import com.imma.persist.annotation.*
import java.util.*

@Suppress("EnumEntryName")
enum class ReportIndicatorArithmetic(val arithmetic: String) {
    none("none"),
    count("count"),
    sum("sum"),
    avg("avg"),
    max("max"),
    min("min");
}

data class ReportIndicator(
    var columnId: String,
    var name: String,
    var arithmetic: ReportIndicatorArithmetic = ReportIndicatorArithmetic.count
)

data class ReportDimension(
    var columnId: String,
    var name: String,
)

data class ReportRect(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f
)

@Entity(CollectionNames.REPORT)
data class Report(
    @Id("_id")
    var reportId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("connect_id")
    var connectId: String? = null,
    @Field("subject_id")
    var subjectId: String? = null,
    @Field("user_id")
    var userId: String? = null,
    @Field("indicators")
    var indicators: List<ReportIndicator> = mutableListOf(),
    @Field("dimensions")
    var dimensions: List<ReportDimension> = mutableListOf(),
    @Field("description")
    var description: String? = null,
    @Field("rect")
    var rect: ReportRect = ReportRect(),
    @Field("chart")
    var chart: Chart = Chart(),
    @Field("last_visit_time")
    var lastVisitTime: String? = null,
    @CreatedAt("_create_time")
    override var createTime: Date? = null,
    @LastModifiedAt("_last_modify_time")
    override var lastModifyTime: Date? = null,
) : Tuple()