package com.imma.service.login

import com.imma.auth.adminEnabled
import com.imma.auth.adminPassword
import com.imma.auth.adminUsername
import com.imma.model.admin.User
import com.imma.service.Service
import com.imma.service.admin.UserCredentialService
import com.imma.service.admin.UserService
import com.imma.utils.getCurrentDateTimeAsString
import io.ktor.application.*
import org.mindrot.jbcrypt.BCrypt

class LoginService(application: Application) : Service(application) {
    fun login(username: String?, plainPassword: String?): User? {
        if (username == null || username.isBlank()) {
            return null
        }

        if (application.adminEnabled
            && username == application.adminUsername
            && plainPassword == application.adminPassword
        ) {
            // successfully login when admin enabled and username/password matched
            return User().apply {
                val now = getCurrentDateTimeAsString()
                userId = username
                name = username
                nickName = username
                active = true
                createTime = now
                lastModifyTime = now
            }
        }

        val user = UserService(application).findUserByName(username)
        val credential = UserCredentialService(application).findCredentialByName(username) ?: return null

        val hashedPassword: String = credential.credential!!
        return if (BCrypt.checkpw(plainPassword, hashedPassword)) {
            user
        } else {
            null
        }
    }
}