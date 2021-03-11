package com.imma.console

import com.imma.auth.Roles
import com.imma.model.Dashboard
import com.imma.service.RouteConstants
import com.imma.utils.isFakeOrNull
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
private fun PipelineContext<Unit, ApplicationCall>.belongsToCurrentUser(
    dashboardId: String?,
    principal: UserIdPrincipal
): Boolean {
    return when {
        // dashboard id is null or a fake id, not exists in persist
        // belongs to current user anyway
        dashboardId.isFakeOrNull() -> true
        // check persist
        else -> DashboardService(application).isDashboardBelongsTo(dashboardId, principal.name)
    }
}

@ExperimentalContracts
fun Route.dashboardSaveByMeRoute() {
    post(RouteConstants.DASHBOARD_SAVE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val dashboard = call.receive<Dashboard>()

        val userId = dashboard.userId
        when {
            // cannot save dashboard which belongs to other user (at least by request data)
            !userId.isNullOrBlank() && userId != principal.name -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use dashboard belongs to others."
            )
            // cannot save dashboard which belongs to other user (check with exists data)
            !belongsToCurrentUser(dashboard.dashboardId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use dashboard belongs to others."
            )
            else -> {
                // assign to current authenticated user
                dashboard.userId = principal.name
                DashboardService(application).saveDashboard(dashboard)
                // remove user id when respond to client
                dashboard.userId = null
                call.respond(dashboard)
            }
        }
    }
}

@ExperimentalContracts
fun Route.dashboardRenameByMeRoute() {
    get(RouteConstants.DASHBOARD_RENAME_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val dashboardId = call.request.queryParameters["dashboard_id"]
        val name = call.request.queryParameters["name"]

        when {
            dashboardId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Dashboard id is required.")
            // cannot save dashboard which belongs to other user (check with exists data)
            !belongsToCurrentUser(dashboardId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use dashboard belongs to others."
            )
            else -> {
                DashboardService(application).renameDashboard(dashboardId, name)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@ExperimentalContracts
fun Route.dashboardDeleteByMeRoute() {
    get(RouteConstants.DASHBOARD_DELETE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val dashboardId = call.request.queryParameters["dashboard_id"]

        when {
            dashboardId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Dashboard id is required.")
            // cannot save dashboard which belongs to other user (check with exists data)
            !belongsToCurrentUser(dashboardId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use dashboard belongs to others."
            )
            else -> {
                DashboardService(application).deleteDashboard(dashboardId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Route.listMyDashboardsRoute() {
    get(RouteConstants.DASHBOARD_LIST_MINE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val dashboards = DashboardService(application).listDashboardByUser(principal.name)
        // remove user id when respond to client
        dashboards.forEach { it.userId = null }
        call.respond(dashboards)
    }
}

@ExperimentalContracts
fun Application.dashboardRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            dashboardSaveByMeRoute()
            dashboardRenameByMeRoute()
            dashboardDeleteByMeRoute()
            listMyDashboardsRoute()
        }
    }
}