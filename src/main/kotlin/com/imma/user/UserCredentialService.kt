package com.imma.user

import com.imma.model.CollectionNames
import com.imma.model.UserCredential
import com.imma.model.assignDateTimePair
import com.imma.service.Service
import io.ktor.application.*
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class UserCredentialService(application: Application) : Service(application) {
    fun saveCredential(credential: UserCredential) {
        // crypt password
        credential.credential = BCrypt.hashpw(credential.credential, BCrypt.gensalt())
        assignDateTimePair(credential)
        writeIntoMongo {
            it.upsert(
                Query.query(Criteria.where("userId").`is`(credential.userId)),
                Update().apply {
                    set("userId", credential.userId)
                    set("name", credential.name)
                    set("credential", credential.credential)
                    set("createTime", credential.createTime)
                    set("lastModifyTime", credential.lastModifyTime)
                    set("lastModified", credential.lastModified)
                },
                UserCredential::class.java,
                CollectionNames.USER_CREDENTIAL
            )
        }
    }

    fun findUserCredentialById(userId: String): UserCredential? {
        return findFromMongo {
            it.findById(userId, UserCredential::class.java, CollectionNames.USER_CREDENTIAL)
        }
    }

    fun findCredentialByName(username: String): UserCredential? {
        return findFromMongo {
            it.findOne(
                Query.query(Criteria.where("name").`is`(username)),
                UserCredential::class.java,
                CollectionNames.USER_CREDENTIAL
            )
        }
    }
}

