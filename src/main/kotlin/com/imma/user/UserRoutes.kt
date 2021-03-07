package com.imma.user

import com.imma.model.User
import com.imma.model.UserForHolder
import com.imma.rest.Pageable
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.saveUserRoute() {
    post("/user") {
        val user = call.receive<User>()
        UserService(application).saveUser(user)
        call.respond(user)
    }
}

fun Route.findUserByIdRoute() {
    get("/user") {
        val userId: String? = call.request.queryParameters["user_id"]
        if (userId == null || userId.isBlank()) {
            // TODO a empty object
            call.respond(mapOf<String, String>())
        } else {
            val user = UserService(application).findUserById(userId)
            if (user == null) {
                // TODO a empty object
                call.respond(mapOf<String, String>())
            } else {
                call.respond(user)
            }
        }
    }
}

fun Route.listUsersByNameRoute() {
    post("/user/name") {
        val pageable = call.receive<Pageable>()
        val name: String? = call.request.queryParameters["query_name"]
        val page = UserService(application).findUsersByName(name, pageable)
        call.respond(page)
    }
}

fun Route.listUsersByNameForHolderRoute() {
    // TODO fix this url
    get("/query/user/group") {
        val name: String? = call.request.queryParameters["query_name"]
        val users = UserService(application).findUsersByNameForHolder(name)
        call.respond(users)
    }
}

fun Route.listUsersByIdsForHolderRoute() {
    post("/user/ids") {
        val userIds: List<String> = call.receive<List<String>>()
        if (userIds.isEmpty()) {
            call.respond(listOf<UserForHolder>())
        } else {
            val users = UserService(application).findUsersByIdsForHolder(userIds)
            call.respond(users)
        }
    }
}

fun Application.userRoutes() {
    routing {
        saveUserRoute()
        findUserByIdRoute()
        listUsersByNameRoute()
        listUsersByNameForHolderRoute()
        listUsersByIdsForHolderRoute()
    }
}