package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Dashboard
import com.imma.model.determineFakeOrNullId
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import kotlin.contracts.ExperimentalContracts

class DashboardService(application: Application) : TupleService(application) {
    @ExperimentalContracts
    fun saveDashboard(dashboard: Dashboard) {
        val fake = determineFakeOrNullId({ dashboard.dashboardId },
            true,
            { dashboard.dashboardId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(dashboard)
        } else {
            updateTuple(dashboard)
        }
    }

    fun renameDashboard(dashboardId: String, name: String?) {
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

    fun isDashboardBelongsTo(dashboardId: String, userId: String): Boolean {
        return getFromMongo {
            it.exists(
                Query.query(Criteria.where("dashboardId").`is`(dashboardId).and("userId").`is`(userId)),
                Dashboard::class.java,
                CollectionNames.DASHBOARD
            )
        }
    }
}

