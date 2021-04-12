package com.imma.utils

class AssumeNeverOccurError(message: String = "An operation is assumed never occurred.") : Error(message)

@Suppress("NOTHING_TO_INLINE")
inline fun neverOccur(): Nothing = throw NotImplementedError()