package com.imma.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
fun String?.isFake(): Boolean {
	return this != null && this.startsWith("f-")
}

@ExperimentalContracts
fun String?.isFakeOrNull(): Boolean {
	contract {
		returns(false) implies (this@isFakeOrNull != null)
	}
	return this == null || this.startsWith("f-")
}