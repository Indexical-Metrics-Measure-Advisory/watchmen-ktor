package com.imma.utils

import io.ktor.application.*
import java.util.concurrent.atomic.AtomicBoolean


class Envs {
	companion object {
		private val initialized = AtomicBoolean()
		private var environment: ApplicationEnvironment? = null

		/**
		 * this method can be called exactly once only.
		 * @see envs
		 */
		fun env(environment: ApplicationEnvironment) {
			if (initialized.get()) {
				throw RuntimeException("Environment was set already, cannot be set twice.")
			}

			if (initialized.compareAndSet(false, true)) {
				this.environment = environment
			}
		}

		private fun env(): ApplicationEnvironment {
			return environment!!
		}

		fun string(key: String): String {
			return env().config.property(key).getString()
		}

		fun stringOrNull(key: String): String? {
			return env().config.propertyOrNull(key)?.getString()
		}

		fun string(key: String, defaultValue: String): String {
			return stringOrNull(key) ?: defaultValue
		}

		fun list(key: String): List<String> {
			return env().config.property(key).getList()
		}

		fun long(key: String): Long {
			return string(key).toLong()
		}

		fun longOrNull(key: String): Long? {
			return stringOrNull(key)?.toLong()
		}

		fun long(key: String, defaultValue: Long): Long {
			return longOrNull(key) ?: defaultValue
		}

		fun int(key: String): Int {
			return string(key).toInt()
		}

		fun intOrNull(key: String): Int? {
			return stringOrNull(key)?.toInt()
		}

		fun int(key: String, defaultValue: Int): Int {
			return intOrNull(key) ?: defaultValue
		}

		fun booleanOrNull(key: String): Boolean? {
			return stringOrNull(key)?.toBoolean()
		}

		fun boolean(key: String, defaultValue: Boolean): Boolean {
			return booleanOrNull(key) ?: defaultValue
		}
	}
}

fun Application.envs(block: (application: Application) -> Unit) {
	Envs.env(this.environment)
	block(this)
}