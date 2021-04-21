package com.imma.model.snowflake

import com.imma.utils.EnvConstants
import com.imma.utils.Envs

/**
 * generate next snow flake id
 */
class SnowflakeHelper {
	companion object {
		private var snowflakeWorker: SnowflakeIdWorker? = null
		fun nextSnowflakeId(): Long {
			if (snowflakeWorker == null) {
				synchronized(this) {
					if (snowflakeWorker == null) {
						val workerId = Envs.long(EnvConstants.SNOWFLAKE_WORKER)
						val dataCenterId = Envs.long(EnvConstants.SNOWFLAKE_DATA_CENTER)
						snowflakeWorker = SnowflakeIdGenerator().createWorker(workerId, dataCenterId, true)
					}
				}
			}
			return snowflakeWorker!!.nextId()
		}
	}
}