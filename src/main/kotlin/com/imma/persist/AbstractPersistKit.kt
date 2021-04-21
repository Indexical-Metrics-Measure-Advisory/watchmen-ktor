package com.imma.persist

import com.imma.model.snowflake.SnowflakeHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory


abstract class AbstractPersistKit : PersistKit {
	private val logger: Logger by lazy {
		LoggerFactory.getLogger(this::class.java)
	}

	protected fun logger(): Logger {
		return logger
	}

	override fun nextSnowflakeId(): Long {
		return SnowflakeHelper.nextSnowflakeId()
	}

	protected fun nextSnowflakeIdStr(): String {
		return this.nextSnowflakeId().toString()
	}
}