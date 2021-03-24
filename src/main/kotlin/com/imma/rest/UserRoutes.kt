package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.admin.User
import com.imma.model.admin.UserForHolder
import com.imma.model.page.Pageable
import com.imma.service.Services
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun Route.saveUserRoute() {
    post(RouteConstants.USER_SAVE) {
        val user = call.receive<User>()
        Services(application).use { it.user { saveUser(user) } }
        call.respond(user)
    }
}

fun Route.findUserByIdRoute() {
    get(RouteConstants.USER_FIND_BY_ID) {
        val userId: String? = call.request.queryParameters["user_id"]
        if (userId == null || userId.isBlank()) {
            // TODO a empty object
            call.respond(mapOf<String, String>())
        } else {
            val user = Services(application).use { it.user { findUserById(userId) } }
            if (user == null) {
                // TODO a empty object
                call.respond(mapOf<String, String>())
            } else {
                call.respond(user)
            }
        }
    }
}

/**
 * TODO it is not compatible with frontend response format, to be continued...
 */
fun Route.listUsersByNameRoute() {
    post(RouteConstants.USER_LIST_BY_NAME) {
        val pageable = call.receive<Pageable>()
        val name: String? = call.request.queryParameters["query_name"]
        val page = Services(application).use { it.user { findUsersByName(name, pageable) } }
        call.respond(page)
    }
}

fun Route.listUsersByNameForHolderRoute() {
    get(RouteConstants.USER_LIST_BY_NAME_FOR_HOLDER) {
        val name: String? = call.request.queryParameters["query_name"]
        val users = Services(application).use { it.user { findUsersByNameForHolder(name) } }
        call.respond(users)
    }
}

fun Route.listUsersByIdsForHolderRoute() {
    post(RouteConstants.USER_LIST_BY_IDS_FOR_HOLDER) {
        val userIds = call.receive<List<String>>()
        if (userIds.isEmpty()) {
            call.respond(listOf<UserForHolder>())
        } else {
            val users = Services(application).use { it.user { findUsersByIdsForHolder(userIds) } }
            call.respond(users)
        }
    }
}

@ExperimentalContracts
fun Application.userRoutes() {
    routing {
        authenticate(Roles.ADMIN.ROLE) {
            saveUserRoute()
            findUserByIdRoute()
            listUsersByNameRoute()
            listUsersByNameForHolderRoute()
            listUsersByIdsForHolderRoute()
        }
    }
}