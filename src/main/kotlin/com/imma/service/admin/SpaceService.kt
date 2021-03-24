package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.admin.Space
import com.imma.model.admin.SpaceForHolder
import com.imma.model.console.AvailableSpace
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.core.select
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import kotlin.contracts.ExperimentalContracts

class SpaceService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveSpace(space: Space) {
        val fake = determineFakeOrNullId({ space.spaceId }, true, { space.spaceId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(space)
        } else {
            updateTuple(space)
        }

        val userGroupIds = space.groupIds
        services.userGroup {
            unassignSpace(space.spaceId!!)
            if (!userGroupIds.isNullOrEmpty()) {
                assignSpace(userGroupIds, space.spaceId!!)
            }
        }
    }

    fun findSpaceById(spaceId: String): Space? {
        return persist().findById(spaceId, Space::class.java, CollectionNames.SPACE)
    }

    fun findSpacesByName(name: String? = "", pageable: Pageable): DataPage<Space> {
        return if (name!!.isEmpty()) {
            persist().page(pageable, Space::class.java, CollectionNames.SPACE)
        } else {
            persist().page(
                where {
                    column("name") regex name
                },
                pageable,
                Space::class.java, CollectionNames.SPACE
            )
        }
    }

    fun findSpacesByNameForHolder(name: String? = ""): List<SpaceForHolder> {
        if (name!!.isEmpty()) {
            return persist().listAll(
                select {
                    include("spaceId")
                    include("name")
                },
                SpaceForHolder::class.java, CollectionNames.SPACE
            )
        } else {
            return persist().list(
                select {
                    include("spaceId")
                    include("name")
                },
                where {
                    column("name") regex name
                },
                SpaceForHolder::class.java, CollectionNames.SPACE
            )
        }
    }

    fun findSpacesByIdsForHolder(spaceIds: List<String>): List<SpaceForHolder> {
        return persist().list(
            select {
                include("spaceId")
                include("name")
            },
            where {
                column("spaceId") `in` spaceIds
            },
            SpaceForHolder::class.java, CollectionNames.SPACE
        )
    }

    fun findAvailableSpaces(userId: String): List<AvailableSpace> {
        val user = services.user { findUserById(userId)!! }
        val userGroupIds = user.groupIds.orEmpty()
        if (userGroupIds.isEmpty()) {
            return mutableListOf()
        }

        val spaceIds = services.userGroup {
            findUserGroupsByIds(userGroupIds).map {
                it.spaceIds.orEmpty()
            }.flatten()
        }
        if (spaceIds.isEmpty()) {
            return mutableListOf()
        }

        return persist().list(
            select {
                include("spaceId")
                include("name")
                include("topicIds")
                include("description")
            },
            where {
                column("spaceId") `in` spaceIds
            },
            AvailableSpace::class.java, CollectionNames.SPACE
        )
    }
}

