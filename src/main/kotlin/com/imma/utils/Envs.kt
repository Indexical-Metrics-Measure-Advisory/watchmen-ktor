package com.imma.utils

import io.ktor.application.*

class Envs {
    companion object {
        internal var environment: ApplicationEnvironment? = null

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

        fun long(key: String): Long {
            return string(key).toLong()
        }

        fun longOrNull(key: String): Long? {
            return env().config.propertyOrNull(key)?.getString()?.toLong()
        }

        fun long(key: String, defaultValue: Long): Long {
            return longOrNull(key) ?: defaultValue
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
    Envs.environment = this.environment
    block(this)
}