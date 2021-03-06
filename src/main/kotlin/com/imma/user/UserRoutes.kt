package com.imma.user

import com.imma.model.User
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
            call.respond(user)
        }
    }
}

fun Route.listUserByNameRoute() {
    post("/user/name") {
        val pageable = call.receive<Pageable>()
        val name: String? = call.request.queryParameters["query_name"]
        val page = UserService(application).findUserByName(name, pageable)
        call.respond(page)
    }
}

fun Application.userRoutes() {
    routing {
        saveUserRoute()
        findUserByIdRoute()
        listUserByNameRoute()
    }
}