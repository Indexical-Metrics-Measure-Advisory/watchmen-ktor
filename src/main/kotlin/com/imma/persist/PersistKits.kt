package com.imma.persist

import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable

abstract class PersistKitProvider(val name: String) {
	abstract fun createKit(): PersistKit
}

/**
 * thread unsafe
 */
class PersistKits : Closeable {
	companion object {
		private const val DEFAULT_KEY: String = "default"
		private val providers: MutableMap<String, PersistKitProvider> = mutableMapOf()

		fun register(provider: PersistKitProvider) {
			providers[provider.name] = provider
		}
	}

	private val logger: Logger by lazy {
		LoggerFactory.getLogger(PersistKits::class.java)
	}
	private val kits: MutableMap<String, PersistKit> = mutableMapOf()
	private val defaultKitName: String by lazy {
		Envs.string(EnvConstants.DEFAULT_PERSIST_KIT, DEFAULT_KEY)
	}

	private fun createKit(key: String): PersistKit {
		val provider = providers[key] ?: throw RuntimeException("Persist kit provider[$key] not found.")
		val kit = provider.createKit()
		kits[key] = kit
		return kit
	}

	/**
	 * current support only one
	 */
	fun select(): PersistKit {
		return kits[defaultKitName] ?: createKit(defaultKitName)
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