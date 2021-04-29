package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.core.Enum
import com.imma.service.Services
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun Route.saveEnumRoute() {
	post(RouteConstants.ENUM_SAVE) {
		val enumeration = call.receive<Enum>()
		Services().use { it.enumeration { saveEnum(enumeration) } }
		call.respond(enumeration)
	}
}

fun Route.findEnumByIdRoute() {
	get(RouteConstants.ENUM_FIND_BY_ID) {
		val enumerationId: String? = call.request.queryParameters["enumeration_id"]
		if (enumerationId == null || enumerationId.isBlank()) {
			// TODO a empty object
			call.respond(mapOf<String, String>())
		} else {
			val enumeration = Services().use { it.enumeration { findEnumById(enumerationId) } }
			if (enumeration == null) {
				// TODO a empty object
				call.respond(mapOf<String, String>())
			} else {
				call.respond(enumeration)
			}
		}
	}
}

fun Route.listEnumsForHolderRoute() {
	post(RouteConstants.ENUM_LIST_FOR_HOLDER) {
		val enumerations = Services().use { it.enumeration { findEnumsForHolder() } }
		call.respond(enumerations)
	}
}

fun Route.findAllEnumsRoute() {
	get(RouteConstants.ENUM_LIST_ALL) {
		val enumerations = Services().use { it.enumeration { findAllEnums() } }
		call.respond(enumerations)
	}
}

@ExperimentalContracts
fun Application.enumRoutes() {
	routing {
		authenticate(Roles.AUTHENTICATED.ROLE) {
			findAllEnumsRoute()
		}
		authenticate(Roles.ADMIN.ROLE) {
			saveEnumRoute()
			findEnumByIdRoute()
			listEnumsForHolderRoute()
		}
	}
}