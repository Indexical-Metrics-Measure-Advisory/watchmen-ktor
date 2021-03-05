package com.imma.user

import com.imma.model.User
import com.imma.model.assignDateTimePair
import com.imma.model.determineFakeId
import com.imma.model.forceAssignDateTimePair
import com.imma.persist.mango.findPageFromMongo
import com.imma.persist.mango.writeIntoMongo
import com.imma.persist.snowflake.nextSnowflakeId
import com.imma.rest.DataPage
import com.imma.rest.Pageable
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query


private fun Application.createUser(user: User) {
    forceAssignDateTimePair(user)
    writeIntoMongo { it.insert(user) }
}

private fun Application.updateUser(user: User) {
    assignDateTimePair(user)
    writeIntoMongo { it.save(user) }
}

fun Application.saveUser(user: User) {
    val fake = determineFakeId(user, true) { nextSnowflakeId().toString() }

    if (fake) {
        createUser(user)
    } else {
        updateUser(user)
    }
}

fun Application.findUserByName(name: String? = "", pageable: Pageable): DataPage<User> {
    val query: Query
    if (name!!.isEmpty()) {
        query = Query.query(Criteria.where("name").all())
    } else {
        query = Query.query(Criteria.where("name").regex(name, "i"))
    }
    return findPageFromMongo(User::class.java, "user", query, pageable)
}
