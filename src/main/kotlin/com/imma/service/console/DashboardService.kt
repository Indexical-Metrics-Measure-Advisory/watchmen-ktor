package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Dashboard
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import kotlin.contracts.ExperimentalContracts

class DashboardService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveDashboard(dashboard: Dashboard) {
        val fake = determineFakeOrNullId({ dashboard.dashboardId },
            true,
            { dashboard.dashboardId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(dashboard, Dashboard::class.java, CollectionNames.DASHBOARD)
        } else {
            updateTuple(dashboard, Dashboard::class.java, CollectionNames.DASHBOARD)
        }
    }

    fun renameDashboard(dashboardId: String, name: String?) {
        persist().updateOne(
            where {
                column("dashboardId") eq dashboardId
            },
            update {
                set("name") to name
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            Dashboard::class.java, CollectionNames.DASHBOARD
        )
    }

    fun deleteDashboard(dashboardId: String) {
        // delete dashboard
        persist().delete(
            where {
                column("dashboardId") eq dashboardId
            },
            Dashboard::class.java, CollectionNames.DASHBOARD
        )
    }

    fun listDashboardByUser(userId: String): List<Dashboard> {
        return persist().list(
            where {
                column("userId") eq userId
            },
            Dashboard::class.java, CollectionNames.DASHBOARD
        )
    }

    fun isDashboardBelongsTo(dashboardId: String, userId: String): Boolean {
        return persist().exists(
            where {
                column("dashboardId") eq dashboardId
                column("userId") eq userId
            },
            Dashboard::class.java, CollectionNames.DASHBOARD
        )
    }
}

