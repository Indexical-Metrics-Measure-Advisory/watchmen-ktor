package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.admin.UserGroup
import com.imma.model.admin.UserGroupForHolder
import com.imma.model.assignDateTimePair
import com.imma.model.determineFakeOrNullId
import com.imma.model.forceAssignDateTimePair
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.service.Service
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import kotlin.contracts.ExperimentalContracts

class UserGroupService(application: Application) : Service(application) {
    private fun createUserGroup(userGroup: UserGroup) {
        forceAssignDateTimePair(userGroup)
        this.writeIntoMongo { it.insert(userGroup) }
    }

    private fun updateUserGroup(userGroup: UserGroup) {
        assignDateTimePair(userGroup)
        writeIntoMongo { it.save(userGroup) }
    }

    @ExperimentalContracts
    fun saveUserGroup(userGroup: UserGroup) {
        val fake = determineFakeOrNullId(
            { userGroup.userGroupId },
            true,
            { userGroup.userGroupId = nextSnowflakeId().toString() })

        if (fake) {
            createUserGroup(userGroup)
        } else {
            updateUserGroup(userGroup)
        }

        val userIds = userGroup.userIds
        val userService = UserService(application)
        userService.unassignUserGroup(userGroup.userGroupId!!)
        if (!userIds.isNullOrEmpty()) {
            userService.assignUserGroup(userIds, userGroup.userGroupId!!)
        }
    }

    fun findUserGroupById(userGroupId: String): UserGroup? {
        return findFromMongo {
            it.findById(userGroupId, UserGroup::class.java, CollectionNames.USER_GROUP)
        }
    }

    fun findUserGroupsByName(name: String? = "", pageable: Pageable): DataPage<UserGroup> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(UserGroup::class.java, CollectionNames.USER_GROUP, query, pageable)
    }

    fun findUserGroupsByNameForHolder(name: String? = ""): List<UserGroupForHolder> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("userGroupId", "name")
        return findListFromMongo(UserGroupForHolder::class.java, CollectionNames.USER_GROUP, query)
    }

    fun findUserGroupsByIds(userGroupIds: List<String>): List<UserGroup> {
        val query: Query = Query.query(Criteria.where("userGroupId").`in`(userGroupIds))
        return findListFromMongo(UserGroup::class.java, CollectionNames.USER_GROUP, query)
    }

    fun findUserGroupsByIdsForHolder(userGroupIds: List<String>): List<UserGroupForHolder> {
        val query: Query = Query.query(Criteria.where("userGroupId").`in`(userGroupIds))
        query.fields().include("userGroupId", "name")
        return findListFromMongo(UserGroupForHolder::class.java, CollectionNames.USER_GROUP, query)
    }

    fun unassignSpace(spaceId: String) {
        writeIntoMongo {
            it.updateMulti(
                Query.query(Criteria.where("spaceIds").`is`(spaceId)),
                Update().apply {
                    pull("spaceIds", spaceId)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                UserGroup::class.java,
                CollectionNames.USER_GROUP
            )
        }
    }

    fun assignSpace(userGroupIds: List<String>, spaceId: String) {
        writeIntoMongo {
            it.updateMulti(
                Query.query(Criteria.where("userGroupId").`in`(userGroupIds)),
                Update().apply {
                    push("spaceIds", spaceId)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                UserGroup::class.java,
                CollectionNames.USER_GROUP
            )
        }
    }
}