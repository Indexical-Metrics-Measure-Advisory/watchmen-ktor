package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.console.LastSnapshot
import com.imma.service.Services
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.findLastSnapshotRoute() {
    get(RouteConstants.LAST_SNAPSHOT) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val lastSnapshot = Services(application).use { it.lastSnapshot { findLastSnapshotById(principal.name) } }
        if (lastSnapshot != null) {
            lastSnapshot.userId = null
            call.respond(lastSnapshot)
        } else {
            call.respond(mapOf<String, String>())
        }
    }
}

fun Route.saveLastSnapshotRoute() {
    post(RouteConstants.LAST_SNAPSHOT_SAVE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val lastSnapshot = call.receive<LastSnapshot>()
        lastSnapshot.userId = principal.name
        Services(application).use { it.lastSnapshot { saveLastSnapshot(lastSnapshot) } }
        lastSnapshot.userId = null
        call.respond(lastSnapshot)
    }
}

fun Application.lastSnapshotRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            findLastSnapshotRoute()
            saveLastSnapshotRoute()
        }
    }
}