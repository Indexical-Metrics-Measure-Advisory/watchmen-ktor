package com.imma

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.imma.auth.*
import com.imma.plugin.PluginLoader
import com.imma.rest.*
import com.imma.service.Services
import com.imma.utils.EnvConstants
import com.imma.utils.envs
import com.imma.utils.isDev
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.slf4j.event.Level
import java.text.SimpleDateFormat
import kotlin.contracts.ExperimentalContracts

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@ExperimentalContracts
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(@Suppress("UNUSED_PARAMETER") testing: Boolean = false) {
    envs {
        PluginLoader.loadPlugins()
    }

    install(CORS) {
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)

            enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)

            setSerializationInclusion(JsonInclude.Include.NON_NULL)

            dateFormat = SimpleDateFormat(environment.config.property(EnvConstants.CONTENT_DATE_FORMAT).getString())
        }
    }

    val jwtVerifier = makeJwtVerifier()
    fun roleBasedAuthorise(authorise: AuthorizationFunction<UserIdPrincipal>): (RoleBaseAuthenticationProvider.Configuration.() -> Unit) {
        return {
            verifier = jwtVerifier
            validate { credentials ->
                val mock = request.queryParameters["mock"]
                if (isDev && mock.equals("true", ignoreCase = true)) {
                    // in development mode, use token as principal
                    // must with query parameter "mock=true", value is case insensitive
                    UserIdPrincipal(credentials.token)
                } else {
                    val userId = verify(credentials.token)
                    if (!userId.isNullOrBlank()) {
                        if (AdminReserved.enabled && userId == AdminReserved.username) {
                            // pass validation when admin enabled and username matched
                            UserIdPrincipal(userId)
                        } else if (Services().use { it.user { isActive(userId) } }) {
                            UserIdPrincipal(userId)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
            }
            authorise { principal ->
                authorise(principal)
            }
        }
    }
    install(Authentication) {
        role(name = "authenticated", roleBasedAuthorise { true })
        role(name = "admin", roleBasedAuthorise { principal ->
            val userId = principal.name
            if (AdminReserved.enabled && userId == AdminReserved.username) {
                // authorise anything when admin enabled and username matched
                true
            } else {
                Services().use { it.user { isAdmin(userId) } }
            }
        })
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }

    routing {
        loginRoutes()
    }

    routing {
        userRoutes()
        userGroupRoutes()

        spaceRoutes()
        topicRoutes()
        enumRoutes()
        pipelineSpaceRoutes()

        connectedSpaceRoutes()
        dashboardRoutes()
        subjectRoutes()
        favoriteRoutes()
        lastSnapshotRoutes()
    }
}
