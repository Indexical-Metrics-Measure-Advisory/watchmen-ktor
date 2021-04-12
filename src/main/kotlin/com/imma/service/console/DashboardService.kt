package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Dashboard
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
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
            where { factor("dashboardId") eq { value(dashboardId) } },
            update { set("name") to name },
            Dashboard::class.java, CollectionNames.DASHBOARD
        )
    }

    fun deleteDashboard(dashboardId: String) {
        // delete dashboard
        persist().deleteById(dashboardId, Dashboard::class.java, CollectionNames.DASHBOARD)
    }

    fun listDashboardByUser(userId: String): List<Dashboard> {
        return persist().list(
            where {
                factor("userId") eq { value(userId) }
            },
            Dashboard::class.java, CollectionNames.DASHBOARD
        )
    }

    fun isDashboardBelongsTo(dashboardId: String, userId: String): Boolean {
        return persist().exists(
            where {
                factor("dashboardId") eq { value(dashboardId) }
                factor("userId") eq { value(userId) }
            },
            Dashboard::class.java, CollectionNames.DASHBOARD
        )
    }
}

