package com.imma.user

import com.imma.model.*
import com.imma.rest.DataPage
import com.imma.rest.Pageable
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class UserGroupService(application: Application) : Service(application) {
    private fun createUserGroup(userGroup: UserGroup) {
        forceAssignDateTimePair(userGroup)
        this.writeIntoMongo { it.insert(userGroup) }
    }

    private fun updateUserGroup(userGroup: UserGroup) {
        assignDateTimePair(userGroup)
        writeIntoMongo { it.save(userGroup) }
    }

    fun saveUserGroup(userGroup: UserGroup) {
        val fake =
            determineFakeId({ userGroup.userGroupId }, true, { userGroup.userGroupId = nextSnowflakeId().toString() })

        if (fake) {
            createUserGroup(userGroup)
        } else {
            updateUserGroup(userGroup)
        }
    }

    fun findUserGroupById(userGroupId: String): UserGroup? {
        return findFromMongo {
            it.findById(userGroupId, UserGroup::class.java, CollectionNames.USER_GROUP)
        }
    }

    fun findUserGroupsByName(name: String? = "", pageable: Pageable): DataPage<UserGroup> {
        val query: Query
        if (name!!.isEmpty()) {
            query = Query.query(Criteria.where("name").all())
        } else {
            query = Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(UserGroup::class.java, CollectionNames.USER_GROUP, query, pageable)
    }

    fun findUserGroupsByNameForHolder(name: String? = ""): List<UserGroupForHolder> {
        val query: Query
        if (name!!.isEmpty()) {
            query = Query.query(Criteria.where("name").all())
        } else {
            query = Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("userGroupId", "name")
        return listPageFromMongo(UserGroupForHolder::class.java, CollectionNames.USER_GROUP, query)
    }

    fun findUserGroupsByIdsForHolder(userGroupIds: List<String>): List<UserGroupForHolder> {
        val query: Query = Query.query(Criteria.where("userGroupId").`in`(userGroupIds))
        query.fields().include("userGroupId", "name")
        return listPageFromMongo(UserGroupForHolder::class.java, CollectionNames.USER_GROUP, query)
    }
}