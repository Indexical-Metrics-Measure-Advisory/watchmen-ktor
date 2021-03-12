package com.imma.user

import com.imma.auth.Roles
import com.imma.model.admin.UserGroup
import com.imma.model.admin.UserGroupForHolder
import com.imma.model.page.Pageable
import com.imma.rest.RouteConstants
import com.imma.service.admin.UserGroupService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun Route.saveUserGroupRoute() {
    post(RouteConstants.USER_GROUP_SAVE) {
        val userGroup = call.receive<UserGroup>()
        UserGroupService(application).saveUserGroup(userGroup)
        call.respond(userGroup)
    }
}

fun Route.findUserGroupByIdRoute() {
    get(RouteConstants.USER_GROUP_FIND_BY_ID) {
        val userGroupId: String? = call.request.queryParameters["user_group_id"]
        if (userGroupId == null || userGroupId.isBlank()) {
            // TODO a empty object
            call.respond(mapOf<String, String>())
        } else {
            val userGroup = UserGroupService(application).findUserGroupById(userGroupId)
            if (userGroup == null) {
                // TODO a empty object
                call.respond(mapOf<String, String>())
            } else {
                call.respond(userGroup)
            }
        }
    }
}

/**
 * TODO it is not compatible with frontend response format, to be continued...
 */
fun Route.listUserGroupsByNameRoute() {
    post(RouteConstants.USER_GROUP_LIST_BY_NAME) {
        val pageable = call.receive<Pageable>()
        val name: String? = call.request.queryParameters["query_name"]
        val page = UserGroupService(application).findUserGroupsByName(name, pageable)
        call.respond(page)
    }
}

fun Route.listUserGroupsByNameForHolderRoute() {
    get(RouteConstants.USER_GROUP_LIST_BY_NAME_FOR_HOLDER) {
        val name: String? = call.request.queryParameters["query_name"]
        val userGroups = UserGroupService(application).findUserGroupsByNameForHolder(name)
        call.respond(userGroups)
    }
}

fun Route.listUserGroupsByIdsForHolderRoute() {
    post(RouteConstants.USER_GROUP_LIST_BY_IDS_FOR_HOLDER) {
        val userGroupIds: List<String> = call.receive<List<String>>()
        if (userGroupIds.isEmpty()) {
            call.respond(listOf<UserGroupForHolder>())
        } else {
            val userGroups = UserGroupService(application).findUserGroupsByIdsForHolder(userGroupIds)
            call.respond(userGroups)
        }
    }
}

@ExperimentalContracts
fun Application.userGroupRoutes() {
    routing {
        authenticate(Roles.ADMIN.ROLE) {
            saveUserGroupRoute()
            findUserGroupByIdRoute()
            listUserGroupsByNameRoute()
            listUserGroupsByNameForHolderRoute()
            listUserGroupsByIdsForHolderRoute()
        }
    }
}