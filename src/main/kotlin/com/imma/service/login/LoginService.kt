package com.imma.service.login

import com.imma.auth.AdminReserved
import com.imma.model.admin.User
import com.imma.service.Service
import com.imma.service.Services
import com.imma.utils.getCurrentDateTime
import org.mindrot.jbcrypt.BCrypt

class LoginService(services: Services) : Service(services) {
	fun login(username: String?, plainPassword: String?): User? {
		if (username == null || username.isBlank()) {
			return null
		}

		if (AdminReserved.enabled
			&& username == AdminReserved.username
			&& plainPassword == AdminReserved.password
		) {
			// successfully login when admin enabled and username/password matched
			return User().apply {
				val now = getCurrentDateTime()
				userId = username
				name = username
				nickName = username
				active = true
				createTime = now
				lastModifyTime = now
			}
		}

		val user = services.user { findUserByName(username) }
		val credential = services.userCredential { findCredentialByName(username) } ?: return null

		val hashedPassword: String = credential.credential!!
		return if (BCrypt.checkpw(plainPassword, hashedPassword)) {
			user
		} else {
			null
		}
	}
}