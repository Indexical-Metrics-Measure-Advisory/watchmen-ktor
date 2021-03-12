package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.admin.Space
import com.imma.model.admin.SpaceForHolder
import com.imma.model.assignDateTimePair
import com.imma.model.console.AvailableSpace
import com.imma.model.determineFakeOrNullId
import com.imma.model.forceAssignDateTimePair
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import kotlin.contracts.ExperimentalContracts

class SpaceService(application: Application) : Service(application) {
    private fun createSpace(space: Space) {
        forceAssignDateTimePair(space)
        this.writeIntoMongo { it.insert(space) }
    }

    private fun updateSpace(space: Space) {
        assignDateTimePair(space)
        writeIntoMongo { it.save(space) }
    }

    @ExperimentalContracts
    fun saveSpace(space: Space) {
        val fake = determineFakeOrNullId({ space.spaceId }, true, { space.spaceId = nextSnowflakeId().toString() })

        if (fake) {
            createSpace(space)
        } else {
            updateSpace(space)
        }

        val userGroupIds = space.groupIds
        val userGroupService = UserGroupService(application)
        userGroupService.unassignSpace(space.spaceId!!)
        if (!userGroupIds.isNullOrEmpty()) {
            userGroupService.assignSpace(userGroupIds, space.spaceId!!)
        }
    }

    fun findSpaceById(spaceId: String): Space? {
        return findFromMongo {
            it.findById(spaceId, Space::class.java, CollectionNames.SPACE)
        }
    }

    fun findSpacesByName(name: String? = "", pageable: Pageable): DataPage<Space> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(Space::class.java, CollectionNames.SPACE, query, pageable)
    }

    fun findSpacesByNameForHolder(name: String? = ""): List<SpaceForHolder> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("spaceId", "name")
        return findListFromMongo(SpaceForHolder::class.java, CollectionNames.SPACE, query)
    }

    fun findSpacesByIdsForHolder(spaceIds: List<String>): List<SpaceForHolder> {
        val query: Query = Query.query(Criteria.where("spaceId").`in`(spaceIds))
        query.fields().include("spaceId", "name")
        return findListFromMongo(SpaceForHolder::class.java, CollectionNames.SPACE, query)
    }

    fun findAvailableSpaces(userId: String): List<AvailableSpace> {
        val user = UserService(application).findUserById(userId)!!
        val userGroupIds = user.groupIds.orEmpty()
        if (userGroupIds.isEmpty()) {
            return mutableListOf()
        }

        val spaceIds = UserGroupService(application).findUserGroupsByIds(userGroupIds).map {
            it.spaceIds.orEmpty()
        }.flatten()
        if (spaceIds.isEmpty()) {
            return mutableListOf()
        }

        val query: Query = Query.query(Criteria.where("spaceId").`in`(spaceIds))
        query.fields().include("spaceId", "name", "topicIds", "description")
        return findListFromMongo(AvailableSpace::class.java, CollectionNames.SPACE, query)
    }
}

