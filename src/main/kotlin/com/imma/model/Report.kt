package com.imma.model

import com.imma.model.chart.Chart
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

enum class ReportIndicatorArithmetic(val arithmetic: String) {
    NONE("none"),
    COUNT("count"),
    SUMMARY("sum"),
    AVERAGE("avg"),
    MAXIMUM("max"),
    MINIMUM("min"),
}

data class ReportIndicator(
    var columnId: String,
    var name: String,
    var arithmetic: ReportIndicatorArithmetic = ReportIndicatorArithmetic.COUNT
)

data class ReportDimension(
    var columnId: String,
    var name: String,
)

@Document(collection = CollectionNames.REPORT)
data class Report(
    @Id
    var reportId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("subject_id")
    var subjectId: String? = null,
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
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple()