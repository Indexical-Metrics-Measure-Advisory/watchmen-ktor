package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.admin.UserGroup
import com.imma.model.admin.UserGroupForHolder
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.core.select
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import kotlin.contracts.ExperimentalContracts

class UserGroupService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveUserGroup(userGroup: UserGroup) {
        val fake = determineFakeOrNullId(
            { userGroup.userGroupId },
            true,
            { userGroup.userGroupId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(userGroup, UserGroup::class.java, CollectionNames.USER_GROUP)
        } else {
            updateTuple(userGroup, UserGroup::class.java, CollectionNames.USER_GROUP)
        }

        val userIds = userGroup.userIds
        services.user {
            unassignUserGroup(userGroup.userGroupId!!)
            if (!userIds.isNullOrEmpty()) {
                assignUserGroup(userIds, userGroup.userGroupId!!)
            }
        }
    }

    fun findUserGroupById(userGroupId: String): UserGroup? {
        return services.persist().findById(userGroupId, UserGroup::class.java, CollectionNames.USER_GROUP)
    }

    fun findUserGroupsByName(name: String?, pageable: Pageable): DataPage<UserGroup> {
        return if (name.isNullOrEmpty()) {
            persist().page(pageable, UserGroup::class.java, CollectionNames.USER_GROUP)
        } else {
            persist().page(
                where {
                    column("name") regex name
                },
                pageable,
                UserGroup::class.java, CollectionNames.USER_GROUP
            )
        }
    }

    fun findUserGroupsByNameForHolder(name: String?): List<UserGroupForHolder> {
        return if (name.isNullOrEmpty()) {
            persist().listAll(
                select {
                    column("userGroupId")
                    column("name")
                },
                UserGroupForHolder::class.java, CollectionNames.USER_GROUP
            )
        } else {
            persist().list(
                select {
                    column("userGroupId")
                    column("name")
                },
                where {
                    column("name") regex name
                },
                UserGroupForHolder::class.java, CollectionNames.USER_GROUP
            )
        }
    }

    fun findUserGroupsByIds(userGroupIds: List<String>): List<UserGroup> {
        return persist().list(
            where {
                column("userGroupId") `in` userGroupIds
            },
            UserGroup::class.java, CollectionNames.USER_GROUP
        )
    }

    fun findUserGroupsByIdsForHolder(userGroupIds: List<String>): List<UserGroupForHolder> {
        return persist().list(
            select {
                column("userGroupId")
                column("name")
            },
            where {
                column("userGroupId") `in` userGroupIds
            },
            UserGroupForHolder::class.java, CollectionNames.USER_GROUP
        )
    }

    fun unassignSpace(spaceId: String) {
        persist().update(
            where {
                column("spaceIds") eq spaceId
            },
            update {
                pull(spaceId) from "spaceIds"
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            UserGroup::class.java, CollectionNames.USER_GROUP
        )
    }

    fun assignSpace(userGroupIds: List<String>, spaceId: String) {
        persist().update(
            where {
                column("userGroupId") `in` userGroupIds
            },
            update {
                push(spaceId) into "spaceIds"
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            UserGroup::class.java, CollectionNames.USER_GROUP
        )
    }
}