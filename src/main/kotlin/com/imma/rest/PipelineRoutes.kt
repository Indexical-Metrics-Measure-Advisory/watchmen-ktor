package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.core.PipelineGraphics
import com.imma.service.core.PipelineGraphicsService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

fun Route.findMyPipelineGraphicsRoute() {
    get(RouteConstants.PIPELINE_GRAPHICS_MINE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val graphics = PipelineGraphicsService(application).findPipelineGraphicsById(principal.name)
        if (graphics != null) {
            // remove user id when respond to client
            graphics.userId = null
            call.respond(graphics)
        } else {
            call.respond(mapOf("topics" to emptyList<String>()))
        }
    }
}

fun Route.saveMyPipelineGraphicsRoute() {
    post(RouteConstants.PIPELINE_GRAPHICS_SAVE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val graphics = call.receive<PipelineGraphics>()
        graphics.userId = principal.name
        PipelineGraphicsService(application).savePipelineGraphicsByUser(graphics)
        // remove user id when respond to client
        graphics.userId = null
        call.respond(graphics)
    }
}

@ExperimentalContracts
fun Application.pipelineSpaceRoutes() {
    routing {
        authenticate(Roles.ADMIN.ROLE) {
            saveMyPipelineGraphicsRoute()
            findMyPipelineGraphicsRoute()
        }
    }
}