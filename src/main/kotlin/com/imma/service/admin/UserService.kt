package com.imma.service.admin

import com.imma.auth.Roles
import com.imma.model.*
import com.imma.model.admin.User
import com.imma.model.admin.UserCredential
import com.imma.model.admin.UserForHolder
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

class UserService(application: Application) : Service(application) {
    private fun updateCredential(user: User) {
        // update credential only when password is given
        if (!user.password.isNullOrEmpty()) {
            val credential = UserCredential().apply {
                userId = user.userId
                name = user.name
                credential = user.password
            }
            UserCredentialService(application).saveCredential(credential)
        }
    }

    private fun createUser(user: User) {
        forceAssignDateTimePair(user)
        this.writeIntoMongo { it.insert(user) }
    }

    private fun updateUser(user: User) {
        assignDateTimePair(user)
        writeIntoMongo { it.save(user) }
    }

    @ExperimentalContracts
    fun saveUser(user: User) {
        val fake = determineFakeOrNullId({ user.userId }, true, { user.userId = nextSnowflakeId().toString() })

        if (fake) {
            createUser(user)
        } else {
            updateUser(user)
        }

        updateCredential(user)
    }

    fun findUserById(userId: String): User? {
        return findFromMongo {
            it.findById(userId, User::class.java, CollectionNames.USER)
        }
    }

    /**
     * exactly match given username
     */
    fun findUserByName(username: String): User? {
        return findFromMongo {
            it.findOne(Query.query(Criteria.where("name").`is`(username)), User::class.java, CollectionNames.USER)
        }
    }

    fun findUsersByName(name: String? = "", pageable: Pageable): DataPage<User> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(User::class.java, CollectionNames.USER, query, pageable)
    }

    fun findUsersByNameForHolder(name: String? = ""): List<UserForHolder> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("userId", "name")
        return findListFromMongo(UserForHolder::class.java, CollectionNames.USER, query)
    }

    fun findUsersByIdsForHolder(userIds: List<String>): List<UserForHolder> {
        val query: Query = Query.query(Criteria.where("userId").`in`(userIds))
        query.fields().include("userId", "name")
        return findListFromMongo(UserForHolder::class.java, CollectionNames.USER, query)
    }

    fun unassignUserGroup(userGroupId: String) {
        writeIntoMongo {
            it.updateMulti(
                Query.query(Criteria.where("groupIds").`is`(userGroupId)),
                Update().apply {
                    pull("groupIds", userGroupId)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                User::class.java,
                CollectionNames.USER
            )
        }
    }

    fun assignUserGroup(userIds: List<String>, userGroupId: String) {
        writeIntoMongo {
            it.updateMulti(
                Query.query(Criteria.where("userId").`in`(userIds)),
                Update().apply {
                    push("groupIds", userGroupId)
                    set("lastModifyTime", getCurrentDateTimeAsString())
                    set("lastModified", getCurrentDateTime())
                },
                User::class.java,
                CollectionNames.USER
            )
        }
    }

    fun isActive(userId: String): Boolean {
        return getFromMongo {
            it.exists(
                Query.query(Criteria.where("userId").`is`(userId).and("active").`is`(true)),
                User::class.java,
                CollectionNames.USER
            )
        }
    }

    fun isAdmin(userId: String): Boolean {
        return getFromMongo {
            it.exists(
                Query.query(Criteria.where("userId").`is`(userId).and("role").`is`(Roles.ADMIN.ROLE)),
                User::class.java,
                CollectionNames.USER
            )
        }
    }
}

