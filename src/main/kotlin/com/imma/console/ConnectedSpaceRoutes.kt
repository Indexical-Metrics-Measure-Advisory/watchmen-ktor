package com.imma.console

import com.imma.auth.Roles
import com.imma.model.ConnectedSpace
import com.imma.service.RouteConstants
import com.imma.utils.isFakeOrNull
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

fun clearUnnecessaryFields(connectedSpace: ConnectedSpace) {
    // remove user id when respond to client
    connectedSpace.userId = null

    connectedSpace.subjects.forEach { subject ->
        subject.connectId = null
        subject.reports.forEach { report ->
            report.subjectId = null
        }
    }
}

@ExperimentalContracts
fun Route.connectSpaceRoute() {
    post(RouteConstants.CONNECT_SPACE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val connectedSpace = call.receive<ConnectedSpace>()

        val userId = connectedSpace.userId
        if (!userId.isNullOrBlank() && userId != principal.name) {
            // cannot save connected space which belongs to other user (at least by request data)
        }

        connectedSpace.connectId.isFakeOrNull()

        // assign to current authenticated user
        connectedSpace.userId = principal.name
        ConnectedSpaceService(application).saveConnectedSpace(connectedSpace)

        clearUnnecessaryFields(connectedSpace)
        call.respond(connectedSpace)
    }
}

//fun Route.connectedSpaceRenameRoute() {
//
//}

@ExperimentalContracts
fun Application.connectedSpaceRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            connectSpaceRoute()
//            connectedSpaceRenameRoute()
        }
    }
}