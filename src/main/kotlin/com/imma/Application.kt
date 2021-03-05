package com.imma

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.imma.user.userRoutes
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import org.slf4j.event.Level
import java.text.SimpleDateFormat

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
    install(ShutDownUrl.ApplicationCallFeature) {
        // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
        shutDownUrl = "/ktor/application/shutdown"
        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
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

            dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        }
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    routing {
        userRoutes()
    }
}

