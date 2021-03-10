package com.imma

import com.auth0.jwt.impl.JWTParser
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.imma.auth.*
import com.imma.login.loginRoutes
import com.imma.user.userGroupRoutes
import com.imma.user.userRoutes
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
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) {
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
//    install(ShutDownUrl.ApplicationCallFeature) {
//        // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
//        shutDownUrl = "/ktor/application/shutdown"
//        // A function that will be executed to get the exit code of the process
//        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
//    }

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

            dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        }
    }

    install(Authentication) {
        role("admin") {
            realm = jwtRealm
            verifier(makeJwtVerifier(jwtIssuer, jwtAudience))
            validate { credentials ->
                if (isDev) {
                    // in development mode, use token as principal
                    UserIdPrincipal(credentials.token)
                } else {
                    val payloadString = String(Base64.getUrlDecoder().decode(credentials.token))
                    val payload = JWTParser().parsePayload(payloadString)
                    if (payload.audience.contains(jwtAudience)) {
                        UserIdPrincipal("hello")
                    } else {
                        null
                    }
                }
            }
        }
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
        authenticate("admin") {
            userRoutes()
            userGroupRoutes()
        }
    }
}

