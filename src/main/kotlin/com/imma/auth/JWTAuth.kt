package com.imma.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.imma.utils.EnvConstants
import io.ktor.application.*
import java.security.SecureRandom
import java.util.*

val Application.jwtIssuer get() = environment.config.property(EnvConstants.JWT_DOMAIN).getString()
val Application.jwtAudience get() = environment.config.property(EnvConstants.JWT_AUDIENCE).getString()
val Application.jwtRealm get() = environment.config.property(EnvConstants.JWT_REALM).getString()

private fun generateRandomSecret(): String {
    val bytes = ByteArray(24)
    SecureRandom().nextBytes(bytes)
    return Base64.getUrlEncoder().encodeToString(bytes)
}

val Application.jwtAlgorithm: Algorithm get() = Algorithm.HMAC256(generateRandomSecret())

fun Application.makeJwtVerifier(): JWTVerifier =
    JWT.require(jwtAlgorithm).withAudience(jwtAudience).withIssuer(jwtIssuer).build()