package com.imma.service.admin

import com.imma.auth.Roles
import com.imma.model.CollectionNames
import com.imma.model.admin.User
import com.imma.model.admin.UserCredential
import com.imma.model.admin.UserForHolder
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.core.change
import com.imma.persist.core.select
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import kotlin.contracts.ExperimentalContracts

class UserService(services: Services) : TupleService(services) {
    private fun updateCredential(user: User) {
        // update credential only when password is given
        if (!user.password.isNullOrEmpty()) {
            val credential = UserCredential().apply {
                userId = user.userId
                name = user.name
                credential = user.password
            }
            services.userCredential {
                saveCredential(credential)
            }
        }
    }

    @ExperimentalContracts
    fun saveUser(user: User) {
        val fake = determineFakeOrNullId({ user.userId }, true, { user.userId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(user)
        } else {
            updateTuple(user)
        }

        updateCredential(user)
    }

    fun findUserById(userId: String): User? {
        return services.persist().findById(userId, User::class.java, CollectionNames.USER)
    }

    /**
     * exactly match given username
     */
    fun findUserByName(username: String): User? {
        return services.persist().findOne(
            where {
                column("name") eq username
            },
            User::class.java, CollectionNames.USER
        )
    }

    fun findUsersByName(name: String?, pageable: Pageable): DataPage<User> {
        return if (name.isNullOrEmpty()) {
            persist().page(pageable, User::class.java, CollectionNames.USER)
        } else {
            persist().page(
                where {
                    column("name") regex name
                },
                pageable,
                User::class.java, CollectionNames.USER
            )
        }
    }

    fun findUsersByNameForHolder(name: String?): List<UserForHolder> {
        return if (name.isNullOrEmpty()) {
            persist().listAll(
                select {
                    include("userId")
                    include("name")
                },
                UserForHolder::class.java, CollectionNames.USER
            )
        } else {
            persist().list(
                select {
                    include("userId")
                    include("name")
                },
                where {
                    column("name") regex name
                },
                UserForHolder::class.java, CollectionNames.USER
            )
        }
    }

    fun findUsersByIdsForHolder(userIds: List<String>): List<UserForHolder> {
        return persist().list(
            select {
                include("userId")
                include("name")
            },
            where {
                column("userId") `in` userIds
            },
            UserForHolder::class.java, CollectionNames.USER
        )
    }

    fun unassignUserGroup(userGroupId: String) {
        persist().update(
            where {
                column("groupIds") include userGroupId
            },
            change {
                pull(userGroupId) from "groupIds"
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            User::class.java, CollectionNames.USER
        )
    }

    fun assignUserGroup(userIds: List<String>, userGroupId: String) {
        persist().update(
            where {
                column("userId") `in` userIds
            },
            change {
                push(userGroupId) into "groupIds"
                set("lastModifyTime") to getCurrentDateTimeAsString()
                set("lastModified") to getCurrentDateTime()
            },
            User::class.java, CollectionNames.USER
        )
    }

    fun isActive(userId: String): Boolean {
        return persist().exists(
            where {
                column("userId") eq userId
                column("active") eq true
            },
            User::class.java, CollectionNames.USER
        )
    }

    fun isAdmin(userId: String): Boolean {
        return persist().exists(
            where {
                column("userId") eq userId
                column("role") eq Roles.ADMIN.ROLE
            },
            User::class.java, CollectionNames.USER
        )
    }
}

