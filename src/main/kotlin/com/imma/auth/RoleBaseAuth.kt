package com.imma.auth

import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.auth.*
import io.ktor.request.*
import io.ktor.response.*

/**
 * Represents a role based authentication provider
 * @property name is the name of the provider, or `null` for a default provider
 */
class RoleBaseAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {
    internal val realm: String = configuration.realm

    internal val authenticationFunction = configuration.authenticationFunction

    /**
     * Role base auth configuration
     */
    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {
        internal var authenticationFunction: AuthenticationFunction<RoleBaseCredential> = {
            throw NotImplementedError(
                "Role base auth validate function is not specified. Use role { validate { ... } } to fix."
            )
        }

        /**
         * Specifies realm to be passed in `WWW-Authenticate` header
         */
        var realm: String = "Ktor Server"
        internal var verifier: ((HttpAuthHeader) -> JWTVerifier?) = { null }

        fun verifier(verifier: JWTVerifier) {
            this.verifier = { verifier }
        }

        /**
         * Sets a validation function that will check given [UserPasswordCredential] instance and return [Principal],
         * or null if credential does not correspond to an authenticated principal
         */
        fun validate(body: suspend ApplicationCall.(RoleBaseCredential) -> Principal?) {
            authenticationFunction = body
        }
    }
}

/**
 * Installs Role Base Authentication mechanism
 */
fun Authentication.Configuration.role(
    name: String? = null,
    configure: RoleBaseAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = RoleBaseAuthenticationProvider(RoleBaseAuthenticationProvider.Configuration(name).apply(configure))
    val realm = provider.realm
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val credentials = call.request.roleBaseAuthenticationCredentials()
        val principal = credentials?.let { authenticate(call, it) }

        val cause = when {
            credentials == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(roleBaseAuthenticationChallengeKey, cause) {
                call.respond(UnauthorizedResponse(HttpAuthHeader.Parameterized(
                    "Bearer",
                    LinkedHashMap<String, String>().apply {
                        put(HttpAuthHeader.Parameters.Realm, realm)
                    }
                )))
                it.complete()
            }
        }
        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}

/**
 * Retrieves Role base authentication credentials for this [ApplicationRequest]
 */
fun ApplicationRequest.roleBaseAuthenticationCredentials(): RoleBaseCredential? {
    when (val authHeader = parseAuthorizationHeader()) {
        is HttpAuthHeader.Single -> {
            if (!authHeader.authScheme.equals("Bearer", ignoreCase = false)) {
                return null
            }

            if (authHeader.blob.isBlank()) {
                return null
            }

            return RoleBaseCredential(authHeader.blob)
        }
        else -> return null
    }
}

private val roleBaseAuthenticationChallengeKey: Any = "RoleBaseAuth"

data class RoleBaseCredential(val token: String) : Credential
