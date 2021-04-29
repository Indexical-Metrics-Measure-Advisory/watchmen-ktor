package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

@Entity(CollectionNames.LAST_SNAPSHOT)
class LastSnapshot(
	@Id
	var userId: String? = null,
	@Field("language")
	var language: String? = null,
	@Field("theme")
	var theme: String? = null,
	@Field("last_dashboard_id")
	var lastDashboardId: String? = null,
	@Field("admin_dashboard_id")
	var adminDashboardId: String? = null,
	@Field("favorite_pin")
	var favoritePin: Boolean? = false
)