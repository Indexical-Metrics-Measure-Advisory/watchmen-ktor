package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

data class DashboardReportRect(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f
)

data class DashboardReport(
    var reportId: String = "",
    var rect: DashboardReportRect = DashboardReportRect()
)

data class ParagraphRect(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f
)

data class Paragraph(
    var content: String = "",
    var rect: ParagraphRect = ParagraphRect()
)

@Document(collection = CollectionNames.DASHBOARD)
data class Dashboard(
    @Id
    var dashboardId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("user_id")
    var userId: String? = null,
    @Field("reports")
    var reports: List<DashboardReport> = mutableListOf(),
    @Field("paragraphs")
    var paragraphs: List<Paragraph> = mutableListOf(),
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