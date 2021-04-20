package com.imma.utils

class AssumeNeverOccurError(message: String = "An operation is assumed never occurred.") : Error(message)

fun neverOccur(): Nothing = throw NotImplementedError()

fun nothing() {}
