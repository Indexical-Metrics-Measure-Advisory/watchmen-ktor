package com.imma.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

private val algorithm = Algorithm.HMAC256("secret")
fun makeJwtVerifier(issuer: String, audience: String): JWTVerifier =
    JWT.require(algorithm).withAudience(audience).withIssuer(issuer).build()