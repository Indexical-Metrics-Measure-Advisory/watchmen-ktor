package com.imma.space

import com.imma.auth.Roles
import com.imma.model.Space
import com.imma.model.SpaceForHolder
import com.imma.rest.Pageable
import com.imma.service.RouteConstants
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun Route.saveSpaceRoute() {
    post(RouteConstants.SPACE_SAVE) {
        val space = call.receive<Space>()
        SpaceService(application).saveSpace(space)
        call.respond(space)
    }
}

fun Route.findSpaceByIdRoute() {
    get(RouteConstants.SPACE_FIND_BY_ID) {
        val spaceId: String? = call.request.queryParameters["space_id"]
        if (spaceId == null || spaceId.isBlank()) {
            // TODO a empty object
            call.respond(mapOf<String, String>())
        } else {
            val space = SpaceService(application).findSpaceById(spaceId)
            if (space == null) {
                // TODO a empty object
                call.respond(mapOf<String, String>())
            } else {
                call.respond(space)
            }
        }
    }
}

fun Route.listSpacesByNameRoute() {
    post(RouteConstants.SPACE_LIST_BY_NAME) {
        val pageable = call.receive<Pageable>()
        val name: String? = call.request.queryParameters["query_name"]
        val page = SpaceService(application).findSpacesByName(name, pageable)
        call.respond(page)
    }
}

fun Route.listSpacesByNameForHolderRoute() {
    get(RouteConstants.SPACE_LIST_BY_NAME_FOR_HOLDER) {
        val name: String? = call.request.queryParameters["query_name"]
        val spaces = SpaceService(application).findSpacesByNameForHolder(name)
        call.respond(spaces)
    }
}

fun Route.listSpacesByIdsForHolderRoute() {
    post(RouteConstants.SPACE_LIST_BY_IDS_FOR_HOLDER) {
        val spaceIds: List<String> = call.receive<List<String>>()
        if (spaceIds.isEmpty()) {
            call.respond(listOf<SpaceForHolder>())
        } else {
            val spaces = SpaceService(application).findSpacesByIdsForHolder(spaceIds)
            call.respond(spaces)
        }
    }
}

@ExperimentalContracts
fun Application.spaceRoutes() {
    routing {
        authenticate(Roles.ADMIN.ROLE) {
            saveSpaceRoute()
            findSpaceByIdRoute()
            listSpacesByNameRoute()
            listSpacesByNameForHolderRoute()
            listSpacesByIdsForHolderRoute()
        }
    }
}