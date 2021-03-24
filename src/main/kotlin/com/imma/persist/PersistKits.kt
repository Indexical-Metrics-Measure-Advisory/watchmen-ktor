package com.imma.persist

import com.imma.utils.EnvConstants
import io.ktor.application.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable

/**
 * thread unsafe
 */
class PersistKits(val application: Application) : Closeable {
    companion object {
        const val DEFAULT_KEY: String = "default"
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(PersistKits::class.java)
    }
    private val kits: MutableMap<String, PersistKit> = mutableMapOf()

    private fun createKit(key: String): PersistKit {
        val env = application.environment
        val dialectClass = env.config.property(EnvConstants.PERSIST_DIALECT_CLASS).getString()
        val constructor = Class.forName(dialectClass).getConstructor(Application::class.java)
        val kit = constructor.newInstance(application) as PersistKit
        kits[key] = kit
        return kit
    }

    /**
     * current support only one
     */
    fun select(): PersistKit {
        return kits[DEFAULT_KEY] ?: createKit(DEFAULT_KEY)
    }

    override fun close() {
        kits.values.forEach {
            try {
                it.close()
            } catch (e: Throwable) {
                // ignore
                logger.error("Failed to close persist kit.", e)
            }
        }
    }
}