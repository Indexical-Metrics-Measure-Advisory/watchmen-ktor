package com.imma.rest

import com.imma.auth.signByJwt
import com.imma.service.Services
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
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
		val user = Services().use { it.auth { login(username, password) } }
		if (user == null) {
			call.respond(HttpStatusCode.BadRequest, "Incorrect username or password.")
		} else if (!user.active) {
			call.respond(HttpStatusCode.BadRequest, "Inactive user.")
		} else {
			val minutes = Envs.long(EnvConstants.TOKEN_EXPIRE_MINUTES)
			val token = signByJwt(user.userId!!, minutes)
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