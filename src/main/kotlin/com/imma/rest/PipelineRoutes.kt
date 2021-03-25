package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.core.Pipeline
import com.imma.model.core.PipelineGraphics
import com.imma.service.Services
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

fun Route.findMyPipelineGraphicsRoute() {
    get(RouteConstants.PIPELINE_GRAPHICS_MINE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val graphics = Services(application).use { it.pipelineGraphics { findPipelineGraphicsById(principal.name) } }
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
        Services(application).use { it.pipelineGraphics { savePipelineGraphicsByUser(graphics) } }
        // remove user id when respond to client
        graphics.userId = null
        call.respond(graphics)
    }
}

@ExperimentalContracts
fun Route.savePipelineRoute() {
    post(RouteConstants.PIPELINE_SAVE) {
        val pipeline = call.receive<Pipeline>()
        Services(application).use { it.pipeline { savePipeline(pipeline) } }
        call.respond(pipeline)
    }
}

fun Route.renamePipelineRoute() {
    get(RouteConstants.PIPELINE_RENAME) {
        val pipelineId = call.request.queryParameters["pipeline_id"]
        val name = call.request.queryParameters["name"]

        when {
            pipelineId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Pipeline id is required.")
            else -> {
                Services(application).use { it.pipeline { renamePipeline(pipelineId, name) } }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Route.togglePipelineEnablementRoute() {
    get(RouteConstants.PIPELINE_ENABLEMENT_TOGGLE) {
        val pipelineId = call.request.queryParameters["pipeline_id"]
        val enabled = call.request.queryParameters["enabled"]

        when {
            pipelineId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Pipeline id is required.")
            else -> {
                Services(application).use { it.pipeline { togglePipelineEnablement(pipelineId, enabled.toBoolean()) } }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Route.findAllPipelinesRoute() {
    get(RouteConstants.PIPELINE_LIST_ALL) {
        val pipelines = Services(application).use { it.pipeline { findAllPipelines() } }
        call.respond(pipelines)
    }
}

@ExperimentalContracts
fun Application.pipelineSpaceRoutes() {
    routing {
        authenticate(Roles.ADMIN.ROLE) {
            savePipelineRoute()
            renamePipelineRoute()
            togglePipelineEnablementRoute()
            findAllPipelinesRoute()
            saveMyPipelineGraphicsRoute()
            findMyPipelineGraphicsRoute()
        }
    }
}