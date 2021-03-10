package com.imma.utils

import io.ktor.application.*

val Application.envKind get() = environment.config.property(EnvConstants.ENV_MODE).getString()
val Application.isDev get() = envKind == EnvConstants.ENV_MODE_DEV
val Application.isProd get() = envKind == EnvConstants.ENV_MODE_PROD