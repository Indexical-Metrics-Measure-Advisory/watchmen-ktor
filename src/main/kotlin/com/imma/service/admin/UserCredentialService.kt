package com.imma.service.admin

import com.imma.model.CollectionNames
import com.imma.model.admin.UserCredential
import com.imma.model.assignDateTimePair
import com.imma.persist.core.where
import com.imma.service.Service
import com.imma.service.Services
import org.mindrot.jbcrypt.BCrypt

class UserCredentialService(services: Services) : Service(services) {
    fun saveCredential(credential: UserCredential) {
        // crypt password
        credential.credential = BCrypt.hashpw(credential.credential, BCrypt.gensalt())
        assignDateTimePair(credential)
        services.persist().upsertOne(credential, UserCredential::class.java, CollectionNames.USER_CREDENTIAL)
    }

    fun findUserCredentialById(userId: String): UserCredential? {
        return services.persist().findById(userId, UserCredential::class.java, CollectionNames.USER_CREDENTIAL)
    }

    fun findCredentialByName(username: String): UserCredential? {
        return services.persist().findOne(
            where {
                factor("name") eq { value(username) }
            },
            UserCredential::class.java,
            CollectionNames.USER_CREDENTIAL
        )
    }
}

