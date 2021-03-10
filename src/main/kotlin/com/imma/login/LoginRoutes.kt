package com.imma.login

import LoginService
import com.imma.service.RouteConstants
import com.imma.utils.EnvConstants
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.loginRoute() {
    post(RouteConstants.LOGIN) {
        val parameters = call.receiveParameters()
        val username = parameters["username"]
        val password = parameters["password"]
        val user = LoginService(application).login(username, password)
        if (user == null) {
            call.respond(HttpStatusCode.BadRequest, "Incorrect username or password.")
        } else if (!user.active) {
            call.respond(HttpStatusCode.BadRequest, "Inactive user.")
        } else {
            val minutes = application.environment.config.property(EnvConstants.TOKEN_EXPIRE_MINUTES)
        }
        call.respond(username + "," + password)
    }
}

fun Application.loginRoutes() {
    routing {
        loginRoute()
    }
}