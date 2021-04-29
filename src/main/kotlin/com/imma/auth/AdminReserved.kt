package com.imma.auth

import com.imma.utils.EnvConstants
import com.imma.utils.Envs

class AdminReserved {
	companion object {
		val username: String?
			get() {
				return Envs.stringOrNull(EnvConstants.ADMIN_USERNAME)
			}

		val password: String?
			get() {
				return Envs.stringOrNull(EnvConstants.ADMIN_PASSWORD)
			}

		val enabled: Boolean
			get() {
				return Envs.boolean(EnvConstants.ADMIN_ENABLED, false)
			}
	}
}