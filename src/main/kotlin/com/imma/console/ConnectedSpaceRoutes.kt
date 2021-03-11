package com.imma.console

import com.imma.auth.Roles
import com.imma.model.ConnectedSpace
import com.imma.model.ConnectedSpaceGraphics
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

fun clearUnnecessaryFields(connectedSpace: ConnectedSpace) {
    // remove user id when respond to client
    connectedSpace.userId = null

    connectedSpace.subjects.forEach { subject ->
        subject.connectId = null
        subject.reports.forEach { report ->
            report.connectId = null
            report.subjectId = null
        }
    }
}

@ExperimentalContracts
private fun PipelineContext<Unit, ApplicationCall>.belongsToCurrentUser(
    connectId: String?,
    principal: UserIdPrincipal
): Boolean {
    return when {
        // connect id is null or a fake id, not exists in persist
        // belongs to current user anyway
        connectId.isFakeOrNull() -> true
        // check persist
        else -> ConnectedSpaceService(application).isConnectedSpaceBelongsTo(connectId, principal.name)
    }
}

@ExperimentalContracts
fun Route.connectSpaceByMeRoute() {
    post(RouteConstants.CONNECT_SPACE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val connectedSpace = call.receive<ConnectedSpace>()

        val userId = connectedSpace.userId
        when {
            // cannot save connected space which belongs to other user (at least by request data)
            !userId.isNullOrBlank() && userId != principal.name -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use connected space belongs to others."
            )
            // cannot save connected space which belongs to other user (check with exists data)
            !belongsToCurrentUser(connectedSpace.connectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use connected space belongs to others."
            )
            else -> {
                // assign to current authenticated user
                connectedSpace.userId = principal.name
                ConnectedSpaceService(application).saveConnectedSpace(connectedSpace)

                clearUnnecessaryFields(connectedSpace)
                call.respond(connectedSpace)
            }
        }
    }
}

@ExperimentalContracts
fun Route.connectedSpaceRenameByMeRoute() {
    get(RouteConstants.CONNECTED_SPACE_RENAME_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val connectId = call.request.queryParameters["connect_id"]
        val name = call.request.queryParameters["name"]

        when {
            connectId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Connected space id is required.")
            // cannot save connected space which belongs to other user (check with exists data)
            !belongsToCurrentUser(connectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use connected space belongs to others."
            )
            else -> {
                ConnectedSpaceService(application).renameConnectedSpace(connectId, name)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@ExperimentalContracts
fun Route.connectedSpaceDeleteByMeRoute() {
    get(RouteConstants.CONNECTED_SPACE_DELETE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val connectId = call.request.queryParameters["connect_id"]

        when {
            connectId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Connected space id is required.")
            // cannot save connected space which belongs to other user (check with exists data)
            !belongsToCurrentUser(connectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use connected space belongs to others."
            )
            else -> {
                ConnectedSpaceService(application).deleteConnectedSpace(connectId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Route.listMyConnectedSpaceRoute() {
    get(RouteConstants.CONNECTED_SPACE_LIST_BY_MINE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val connectedSpaces = ConnectedSpaceService(application).listConnectedSpaceByUser(principal.name)
        connectedSpaces.forEach { clearUnnecessaryFields(it) }
        call.respond(connectedSpaces)
    }
}

@ExperimentalContracts
fun Route.saveConnectedSpaceGraphicsByMeRoute() {
    post(RouteConstants.CONNECTED_SPACE_GRAPHICS_SAVE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val graphics = call.receive<ConnectedSpaceGraphics>()

        val userId = graphics.userId
        val connectId = graphics.connectId

        when {
            connectId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Connected space id is required.")
            connectId.isFakeOrNull() -> call.respond(
                HttpStatusCode.BadRequest,
                "Fake connected space id is not allowed."
            )
            // cannot save connected space which belongs to other user (at least by request data)
            !userId.isNullOrBlank() && userId != principal.name -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use connected space belongs to others."
            )
            // cannot save connected space which belongs to other user (check with exists data)
            !belongsToCurrentUser(connectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use connected space belongs to others."
            )
            else -> {
                graphics.userId = principal.name
                ConnectedSpaceGraphicsService(application).saveConnectedSpaceGraphics(graphics)
                call.respond(graphics)
            }
        }
    }
}

fun Route.listMyConnectedSpaceGraphicsRoute() {
    get(RouteConstants.CONNECTED_SPACE_GRAPHICS_LIST_BY_MINE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val graphics = ConnectedSpaceGraphicsService(application).listConnectedSpaceGraphicsByUser(principal.name)
        // remove user id when respond to client
        graphics.forEach { it.userId = null }
        call.respond(graphics)
    }
}

@ExperimentalContracts
fun Application.connectedSpaceRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            connectSpaceByMeRoute()
            connectedSpaceRenameByMeRoute()
            connectedSpaceDeleteByMeRoute()
            listMyConnectedSpaceRoute()
            saveConnectedSpaceGraphicsByMeRoute()
            listMyConnectedSpaceGraphicsRoute()
        }
    }
}