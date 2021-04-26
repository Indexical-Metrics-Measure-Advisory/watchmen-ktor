package com.imma.service.admin

import com.imma.auth.Roles
import com.imma.model.CollectionNames
import com.imma.model.admin.User
import com.imma.model.admin.UserCredential
import com.imma.model.admin.UserForHolder
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.core.select
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
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
            createTuple(user, User::class.java, CollectionNames.USER)
        } else {
            updateTuple(user, User::class.java, CollectionNames.USER)
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
                factor("name") eq { value(username) }
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
                    factor("name") hasText { value(name) }
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
                    factor("userId")
                    factor("name")
                },
                UserForHolder::class.java, CollectionNames.USER
            )
        } else {
            persist().list(
                select {
                    factor("userId")
                    factor("name")
                },
                where {
                    factor("name") hasText { value(name) }
                },
                UserForHolder::class.java, CollectionNames.USER
            )
        }
    }

    fun findUsersByIdsForHolder(userIds: List<String>): List<UserForHolder> {
        return persist().list(
            select {
                factor("userId")
                factor("name")
            },
            where {
                factor("userId") existsIn { value(userIds) }
            },
            UserForHolder::class.java, CollectionNames.USER
        )
    }

    fun unassignUserGroup(userGroupId: String) {
        persist().update(
            where {
                factor("groupIds") hasOne { value(userGroupId) }
            },
            update {
                pull(userGroupId) from "groupIds"
            },
            User::class.java, CollectionNames.USER
        )
    }

    fun assignUserGroup(userIds: List<String>, userGroupId: String) {
        persist().update(
            where {
                factor("userId") existsIn { value(userIds) }
            },
            update {
                push(userGroupId) into "groupIds"
            },
            User::class.java, CollectionNames.USER
        )
    }

    fun isActive(userId: String): Boolean {
        return persist().exists(
            where {
                factor("userId") eq { value(userId) }
                factor("active") eq { value(true) }
            },
            User::class.java, CollectionNames.USER
        )
    }

    fun isAdmin(userId: String): Boolean {
        return persist().exists(
            where {
                factor("userId") eq { value(userId) }
                factor("role") eq { value { Roles.ADMIN.ROLE } }
            },
            User::class.java, CollectionNames.USER
        )
    }
}

