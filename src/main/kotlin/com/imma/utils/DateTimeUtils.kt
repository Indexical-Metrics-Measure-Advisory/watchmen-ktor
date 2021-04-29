package com.imma.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun getCurrentDateTime(): Date {
	return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
}

fun getCurrentDateTimeAsString(): String {
	return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getCurrentDateTime())
}
