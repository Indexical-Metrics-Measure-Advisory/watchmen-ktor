package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.core.Topic
import com.imma.model.core.TopicForHolder
import com.imma.model.page.Pageable
import com.imma.service.Services
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun Route.saveTopicRoute() {
    post(RouteConstants.TOPIC_SAVE) {
        val topic = call.receive<Topic>()
        Services().use { it.topic { saveTopic(topic) } }
        call.respond(topic)
    }
}

fun Route.findTopicByIdRoute() {
    get(RouteConstants.TOPIC_FIND_BY_ID) {
        val topicId: String? = call.request.queryParameters["topic_id"]
        if (topicId == null || topicId.isBlank()) {
            // TODO a empty object
            call.respond(mapOf<String, String>())
        } else {
            val topic = Services().use { it.topic { findTopicById(topicId) } }
            if (topic == null) {
                // TODO a empty object
                call.respond(mapOf<String, String>())
            } else {
                call.respond(topic)
            }
        }
    }
}

/**
 * TODO it is not compatible with frontend response format, to be continued...
 */
fun Route.listTopicsByNameRoute() {
    post(RouteConstants.TOPIC_LIST_BY_NAME) {
        val pageable = call.receive<Pageable>()
        val name: String? = call.request.queryParameters["query_name"]
        val page = Services().use { it.topic { findTopicsByName(name, pageable) } }
        call.respond(page)
    }
}

fun Route.listTopicsByNameForHolderRoute() {
    get(RouteConstants.TOPIC_LIST_BY_NAME_FOR_HOLDER) {
        val name: String? = call.request.queryParameters["query_name"]
        val topics = Services().use { it.topic { findTopicsByNameForHolder(name) } }
        call.respond(topics)
    }
}

fun Route.listTopicsByIdsForHolderRoute() {
    post(RouteConstants.TOPIC_LIST_BY_IDS_FOR_HOLDER) {
        val topicIds: List<String> = call.receive<List<String>>()
        if (topicIds.isEmpty()) {
            call.respond(listOf<TopicForHolder>())
        } else {
            val topics = Services().use { it.topic { findTopicsByIdsForHolder(topicIds) } }
            call.respond(topics)
        }
    }
}

fun Route.findAllTopicsRoute() {
    get(RouteConstants.TOPIC_LIST_ALL) {
        val topics = Services().use { it.topic { findAllTopics() } }
        call.respond(topics)
    }
}

@ExperimentalContracts
fun Application.topicRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            listTopicsByIdsForHolderRoute()
        }
        authenticate(Roles.ADMIN.ROLE) {
            saveTopicRoute()
            findTopicByIdRoute()
            listTopicsByNameRoute()
            listTopicsByNameForHolderRoute()
            findAllTopicsRoute()
        }
    }
}