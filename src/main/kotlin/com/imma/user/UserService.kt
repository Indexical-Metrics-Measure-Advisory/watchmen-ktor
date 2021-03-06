package com.imma.user

import com.imma.model.User
import com.imma.model.assignDateTimePair
import com.imma.model.determineFakeId
import com.imma.model.forceAssignDateTimePair
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
        val fake = determineFakeId(user, true) { nextSnowflakeId().toString() }

        if (fake) {
            createUser(user)
        } else {
            updateUser(user)
        }
    }

    fun findUserById(userId: String): User? {
        return findFromMongo {
            it.findById(userId, User::class.java, "user")
        }
    }

    fun findUsersByName(name: String? = "", pageable: Pageable): DataPage<User> {
        val query: Query
        if (name!!.isEmpty()) {
            query = Query.query(Criteria.where("name").all())
        } else {
            query = Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(User::class.java, "user", query, pageable)
    }

    fun findUsersByNameForHolder(name: String? = ""): List<User> {
        val query: Query
        if (name!!.isEmpty()) {
            query = Query.query(Criteria.where("name").all())
        } else {
            query = Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("userId", "name")
        return listPageFromMongo(User::class.java, "user", query)
    }
}

