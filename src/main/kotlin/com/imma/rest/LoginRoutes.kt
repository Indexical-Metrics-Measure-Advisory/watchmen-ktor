package com.imma.rest

import com.auth0.jwt.JWT
import com.imma.auth.jwtAlgorithm
import com.imma.auth.jwtAudience
import com.imma.auth.jwtIssuer
import com.imma.service.Services
import com.imma.utils.EnvConstants
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun Route.loginRoute() {
    post(RouteConstants.LOGIN) {
        val parameters = call.receiveParameters()
        val username = parameters["username"]
        val password = parameters["password"]
        val user = Services().use { it.auth { login(username, password) } }
        if (user == null) {
            call.respond(HttpStatusCode.BadRequest, "Incorrect username or password.")
        } else if (!user.active) {
            call.respond(HttpStatusCode.BadRequest, "Inactive user.")
        } else {
            val minutes =
                application.environment.config.property(EnvConstants.TOKEN_EXPIRE_MINUTES).getString().toLong()
            val now = LocalDateTime.now()
            val expired = now.plusMinutes(minutes)
            val token = JWT.create().withIssuer(application.jwtIssuer)
                .withAudience(application.jwtAudience)
                .withIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .withExpiresAt(Date.from(expired.atZone(ZoneId.systemDefault()).toInstant()))
                .withSubject(user.userId)
                .sign(application.jwtAlgorithm)
            val response = mutableMapOf<String, String>()
            response["access_token"] = token
            call.respond(response)
        }
    }
}

fun Application.loginRoutes() {
    routing {
        loginRoute()
    }
}