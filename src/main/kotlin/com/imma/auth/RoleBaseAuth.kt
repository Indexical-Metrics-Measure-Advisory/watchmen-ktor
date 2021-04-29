package com.imma.auth

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.auth.*
import io.ktor.request.*
import io.ktor.response.*
import java.util.*

typealias AuthorizationFunction<P> = suspend ApplicationCall.(principal: P) -> Boolean

/**
 * Represents a role based authentication provider
 * @property name is the name of the provider, or `null` for a default provider
 */
class RoleBaseAuthenticationProvider internal constructor(
	configuration: Configuration
) : AuthenticationProvider(configuration) {
	internal val authenticationFunction = configuration.authenticationFunction
	internal val authorizationFunction = configuration.authorizationFunction

	/**
	 * Role base auth configuration
	 */
	class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {
		internal var authenticationFunction: AuthenticationFunction<RoleBaseCredential> = {
			throw NotImplementedError(
				"Role base auth validate function is not specified. Use role { validate { ... } } to fix."
			)
		}
		internal var authorizationFunction: AuthorizationFunction<UserIdPrincipal> = {
			throw NotImplementedError(
				"Role base auth authorise function is not specified. Use role { authorise { ... } } to fix."
			)
		}

		internal var verifier: JWTVerifier? = null

		fun verify(token: String): String? {
			val jwt: DecodedJWT? = verifier?.verify(token)
			return if (jwt != null) {
				val payloadString = String(Base64.getUrlDecoder().decode(jwt.payload))
				JWTParser().parsePayload(payloadString).subject
			} else {
				null
			}
		}

		/**
		 * Sets a validation function that will check given [UserPasswordCredential] instance and return [Principal],
		 * or null if credential does not correspond to an authenticated principal
		 */
		fun validate(body: suspend ApplicationCall.(RoleBaseCredential) -> Principal?) {
			authenticationFunction = body
		}

		fun authorise(body: suspend ApplicationCall.(UserIdPrincipal) -> Boolean) {
			authorizationFunction = body
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
	val authenticate = provider.authenticationFunction
	val authorise = provider.authorizationFunction

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
						put(HttpAuthHeader.Parameters.Realm, jwtRealm)
					}
				)))
				it.complete()
			}
		}
		if (principal != null) {
			val pass = authorise(call, principal as UserIdPrincipal)
			if (pass) {
				context.principal(principal)
			} else {
				context.challenge(
					roleBaseAuthenticationChallengeKey,
					AuthenticationFailedCause.Error("Access denied.")
				) {
					call.respond(ForbiddenResponse(HttpAuthHeader.Parameterized(
						"Bearer",
						LinkedHashMap<String, String>().apply {
							put(HttpAuthHeader.Parameters.Realm, jwtRealm)
						}
					)))
					it.complete()
				}
			}
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
