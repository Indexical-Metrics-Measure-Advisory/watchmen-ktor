package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
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

@Entity(CollectionNames.DASHBOARD)
data class Dashboard(
    @Id
    var dashboardId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("user_id")
    var userId: String? = null,
    @Field("auto_refresh_interval")
    var autoRefreshInterval: Boolean? = null,
    @Field("reports")
    var reports: List<DashboardReport> = mutableListOf(),
    @Field("paragraphs")
    var paragraphs: List<Paragraph> = mutableListOf(),
    @Field("last_visit_time")
    var lastVisitTime: String? = null,
    @CreatedAt
    override var createTime: Date? = null,
    @LastModifiedAt
    override var lastModifyTime: Date? = null,
) : Tuple()