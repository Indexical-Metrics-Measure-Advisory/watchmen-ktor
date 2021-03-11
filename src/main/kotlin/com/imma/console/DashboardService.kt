package com.imma.console

import com.imma.model.*
import com.imma.service.Service
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import kotlin.contracts.ExperimentalContracts

class DashboardService(application: Application) : Service(application) {
    private fun createDashboard(dashboard: Dashboard) {
        forceAssignDateTimePair(dashboard)
        this.writeIntoMongo { it.insert(dashboard) }
    }

    private fun updateDashboard(dashboard: Dashboard) {
        assignDateTimePair(dashboard)
        writeIntoMongo { it.save(dashboard) }
    }

    @ExperimentalContracts
    fun saveDashboard(dashboard: Dashboard) {
        val fake = determineFakeOrNullId({ dashboard.dashboardId },
            true,
            { dashboard.dashboardId = nextSnowflakeId().toString() })

        if (fake) {
            createDashboard(dashboard)
        } else {
            updateDashboard(dashboard)
        }
    }

    fun isDashboardBelongsTo(dashboardId: String, userId: String): Boolean {
        return getFromMongo {
            it.exists(
                Query.query(Criteria.where("dashboardId").`is`(dashboardId).and("userId").`is`(userId)),
                Dashboard::class.java,
                CollectionNames.DASHBOARD
            )
        }
    }

    fun renameDashboard(dashboardId: String, name: String? = "") {
        writeIntoMongo {
            it.updateFirst(
                Query.query(Criteria.where("dashboardId").`is`(dashboardId)),
                Update().apply {
                    set("name", name)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                Dashboard::class.java,
                CollectionNames.DASHBOARD
            )
        }
    }

    fun deleteDashboard(dashboardId: String) {
        writeIntoMongo {
            // delete dashboard
            it.remove(
                Query.query(Criteria.where("dashboardId").`is`(dashboardId)),
                Dashboard::class.java,
                CollectionNames.DASHBOARD
            )
        }
    }

    fun listDashboardByUser(userId: String): List<Dashboard> {
        val query: Query = Query.query(Criteria.where("userId").`is`(userId))
        return findListFromMongo(Dashboard::class.java, CollectionNames.DASHBOARD, query)
    }
}

