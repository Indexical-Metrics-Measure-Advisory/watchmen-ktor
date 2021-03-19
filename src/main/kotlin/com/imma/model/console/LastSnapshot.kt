package com.imma.model.console

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = CollectionNames.LAST_SNAPSHOT)
class LastSnapshot(
    @Id
    var userId: String? = null,
    @Field("language")
    var language: String? = null,
    @Field("last_dashboard_id")
    var lastDashboardId: String? = null,
    @Field("admin_dashboard_id")
    var adminDashboardId: String? = null,
    @Field("favorite_pin")
    var favoritePin: Boolean? = false
)