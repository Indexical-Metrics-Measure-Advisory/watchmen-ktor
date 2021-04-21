package com.imma.model.core.compute

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ValueKits {
	companion object {
		fun computeToNumeric(value: Any?, throws: () -> String): BigDecimal? {
			return if (value == null) {
				value
			} else {
				try {
					when (value) {
						is BigDecimal -> value
						is BigInteger -> BigDecimal(value)
						is Int -> BigDecimal(value)
						is Long -> BigDecimal(value)
						else -> BigDecimal(value.toString())
					}
				} catch (t: Throwable) {
					throw RuntimeException(throws())
				}
			}
		}

		fun removeIrrelevantCharsFromDateString(date: String): String {
			return date.split("").filter {
				it != " " && it != "-" && it != "/" && it != ":"
			}.joinToString(separator = "")
		}

		private fun computeToDate(date: String, pattern: String, removeIrrelevantChars: Boolean = false): LocalDate {
			return if (removeIrrelevantChars) {
				LocalDate.parse(removeIrrelevantCharsFromDateString(date), DateTimeFormatter.ofPattern(pattern))
			} else {
				LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern))
			}
		}

		fun computeToDate(date: Any?, throws: () -> String): LocalDate? {
			return when (date) {
				null -> null
				is Date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
				is LocalDate -> date
				is LocalDateTime -> date.toLocalDate()
				is String -> {
					val length = date.length
					when {
						// format is yyyyMMdd
						length == 8 -> computeToDate(date, "yyyyMMdd")
						// format is yyyy/MM/dd, yyyy-MM-dd
						length == 10 -> computeToDate(date, "yyyyMMdd", true)
						// format is yyyyMMddHHmmss
						length == 14 -> computeToDate(date.substring(0, 8), "yyyyMMddHHmmss")
						// format is yyyyMMdd HHmmss
						length == 15 -> computeToDate(date.substring(0, 8), "yyyyMMdd HHmmss")
						// date format is yyyy/MM/dd, yyyy-MM-dd
						// time format is HH:mm:ss
						length >= 18 -> computeToDate(date.substring(0, 10), "yyyyMMddHHmmss", true)
						else -> throw RuntimeException(throws())
					}
				}
				else -> throw RuntimeException(throws())
			}
		}

		fun computeToCollection(value: Any?, throws: () -> String): List<Any?> {
			return when (value) {
				null -> listOf()
				is List<*> -> value
				is Array<*> -> value.toList()
				is String -> value.split(",")
				else -> throw RuntimeException(throws())
			}
		}
	}
}