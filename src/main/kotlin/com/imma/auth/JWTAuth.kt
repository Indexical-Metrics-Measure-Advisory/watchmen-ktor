package com.imma.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.imma.utils.EnvConstants
import io.ktor.application.*

val Application.jwtIssuer get() = environment.config.property(EnvConstants.JWT_DOMAIN).getString()
val Application.jwtAudience get() = environment.config.property(EnvConstants.JWT_AUDIENCE).getString()
val Application.jwtRealm get() = environment.config.property(EnvConstants.JWT_REALM).getString()
val Application.jwtAlgorithm
    get() = Algorithm.HMAC256(
        environment.config.property(EnvConstants.JWT_HMAC256_SECRET).getString()
    )

fun Application.makeJwtVerifier(): JWTVerifier =
    JWT.require(jwtAlgorithm).withAudience(jwtAudience).withIssuer(jwtIssuer).build()