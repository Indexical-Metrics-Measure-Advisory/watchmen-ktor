package com.imma.user

import com.imma.model.*
import com.imma.rest.DataPage
import com.imma.rest.Pageable
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class UserService(application: Application) : Service(application) {
    private fun createUser(user: User) {
        forceAssignDateTimePair(user)
        this.writeIntoMongo { it.insert(user) }
    }

    private fun updateUser(user: User) {
        assignDateTimePair(user)
        writeIntoMongo { it.save(user) }
    }

    fun saveUser(user: User) {
        val fake = determineFakeId({ user.userId }, true, { user.userId = nextSnowflakeId().toString() })

        if (fake) {
            createUser(user)
        } else {
            updateUser(user)
        }
    }

    fun findUserById(userId: String): User? {
        return findFromMongo {
            it.findById(userId, User::class.java, CollectionNames.USER)
        }
    }

    fun findUsersByName(name: String? = "", pageable: Pageable): DataPage<User> {
        val query: Query
        if (name!!.isEmpty()) {
            query = Query.query(Criteria.where("name").all())
        } else {
            query = Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(User::class.java, CollectionNames.USER, query, pageable)
    }

    fun findUsersByNameForHolder(name: String? = ""): List<UserForHolder> {
        val query: Query
        if (name!!.isEmpty()) {
            query = Query.query(Criteria.where("name").all())
        } else {
            query = Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("userId", "name")
        return findListFromMongo(UserForHolder::class.java, CollectionNames.USER, query)
    }

    fun findUsersByIdsForHolder(userIds: List<String>): List<UserForHolder> {
        val query: Query = Query.query(Criteria.where("userId").`in`(userIds))
        query.fields().include("userId", "name")
        return findListFromMongo(UserForHolder::class.java, CollectionNames.USER, query)
    }
}

