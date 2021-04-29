package com.imma.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private val jwtIssuer get() = Envs.string(EnvConstants.JWT_DOMAIN)
private val jwtAudience get() = Envs.string(EnvConstants.JWT_AUDIENCE)

@Suppress("unused")
val jwtRealm
	get() = Envs.string(EnvConstants.JWT_REALM)

private fun generateRandomSecret(): String {
	val bytes = ByteArray(24)
	SecureRandom().nextBytes(bytes)
	return Base64.getUrlEncoder().encodeToString(bytes)
}

private val algorithm: Algorithm = Algorithm.HMAC256(generateRandomSecret())
private val jwtAlgorithm: Algorithm get() = algorithm
fun makeJwtVerifier(): JWTVerifier =
	JWT.require(jwtAlgorithm).withAudience(jwtAudience).withIssuer(jwtIssuer).build()

fun signByJwt(subject: String, minutes: Long): String {
	val now = LocalDateTime.now()
	val expired = now.plusMinutes(minutes)
	return JWT.create().withIssuer(jwtIssuer)
		.withAudience(jwtAudience)
		.withIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
		.withExpiresAt(Date.from(expired.atZone(ZoneId.systemDefault()).toInstant()))
		.withSubject(subject)
		.sign(jwtAlgorithm)
}