package com.imma.service.core

abstract class EngineWorker {
	private var startTime: Long = 0

	protected fun markStart() {
		startTime = System.nanoTime()
	}

	protected fun markEnd(): Double {
		return (System.nanoTime() - startTime.toDouble()) / 1000
	}
}