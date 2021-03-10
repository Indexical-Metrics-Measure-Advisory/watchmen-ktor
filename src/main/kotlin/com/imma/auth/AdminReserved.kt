package com.imma.auth

import com.imma.utils.EnvConstants
import io.ktor.application.*

val Application.adminUsername get() = environment.config.propertyOrNull(EnvConstants.ADMIN_USERNAME)?.getString()
val Application.adminPassword get() = environment.config.propertyOrNull(EnvConstants.ADMIN_PASSWORD)?.getString()
val Application.adminEnabled
    get() = environment.config.propertyOrNull(EnvConstants.ADMIN_ENABLED)?.getString().toBoolean()
