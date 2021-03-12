package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.console.Favorite
import com.imma.service.console.FavoriteService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.findFavoriteRoute() {
    get(RouteConstants.FAVORITE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val favorite = FavoriteService(application).findFavoriteById(principal.name)
        if (favorite != null) {
            favorite.userId = null
            call.respond(favorite)
        } else {
            call.respond(
                mapOf(
                    "connectedSpaceIds" to listOf<String>(),
                    "dashboardIds" to listOf()
                )
            )
        }
    }
}

fun Route.saveFavoriteRoute() {
    post(RouteConstants.FAVORITE_SAVE) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val favorite = call.receive<Favorite>()
        favorite.userId = principal.name
        FavoriteService(application).saveFavorite(favorite)
        favorite.userId = null
        call.respond(favorite)
    }
}

fun Application.favoriteRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            findFavoriteRoute()
            saveFavoriteRoute()
        }
    }
}