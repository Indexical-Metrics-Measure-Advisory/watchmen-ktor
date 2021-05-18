package com.imma.model.core.compute

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ValueKits {
    companion object {
        fun computeToSequence(value: Any?, throws: () -> String): Long {
            try {
                return value.toString().toLong()
            } catch (t: Throwable) {
                throw RuntimeException(throws(), t)
            }
        }

        fun computeToBoolean(value: Any?, throws: () -> String): Boolean {
            return when (value) {
                null -> false
                is Boolean -> value
                is Number -> value.toInt() > 0
                is BigDecimal -> value.toInt() > 0
                is BigInteger -> value.toInt() > 0
                else -> {
                    val v = value.toString()
                    if ("true,t,yes,y".contains(v, true)) {
                        true
                    } else {
                        try {
                            v.toInt() > 0
                        } catch (t: Throwable) {
                            throw RuntimeException(throws(), t)
                        }
                    }
                }
            }
        }

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
                    throw RuntimeException(throws(), t)
                }
            }
        }

        fun removeIrrelevantCharsFromDateString(date: String): String {
            return date.split("").filter { !" -/:.TZ".contains(it) }.joinToString("")
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
                    val length = date.trim().length
                    when {
                        length == 0 -> null
                        // format is yyyyMMdd
                        length == 8 -> computeToDate(date, "yyyyMMdd")
                        // format is yyyy/MM/dd, yyyy-MM-dd
                        length == 10 -> computeToDate(date, "yyyyMMdd", true)
                        // format is yyyyMMddHHmmss
                        length == 14 -> computeToDate(date.substring(0, 8), "yyyyMMdd")
                        // format is yyyyMMdd HHmmss
                        length == 15 -> computeToDate(date.substring(0, 8), "yyyyMMdd")
                        // date format is yyyy/MM/dd, yyyy-MM-dd
                        // time format is HH:mm:ss
                        length >= 18 -> computeToDate(date.substring(0, 10), "yyyyMMdd", true)
                        else -> throw RuntimeException(throws())
                    }
                }
                else -> throw RuntimeException(throws())
            }
        }

        private fun computeToTime(
            dateOrTime: String,
            pattern: String,
            removeIrrelevantChars: Boolean = false
        ): LocalTime {
            return if (removeIrrelevantChars) {
                LocalTime.parse(removeIrrelevantCharsFromDateString(dateOrTime), DateTimeFormatter.ofPattern(pattern))
            } else {
                LocalTime.parse(dateOrTime, DateTimeFormatter.ofPattern(pattern))
            }
        }

        fun computeToTime(dateOrTime: Any?, throws: () -> String): LocalTime? {
            return when (dateOrTime) {
                null -> null
                is Date -> dateOrTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                is LocalDate -> LocalTime.of(0, 0, 0)
                is LocalDateTime -> dateOrTime.toLocalTime()
                is String -> {
                    val length = dateOrTime.length
                    when {
                        // format is HHmmss
                        length == 6 -> computeToTime(dateOrTime, "HHmmss")
                        // format is yyyyMMddHHmmss
                        length == 14 -> computeToTime(dateOrTime.substring(8), "HHmmss")
                        // format is yyyyMMdd HHmmss
                        length == 15 -> computeToTime(dateOrTime.substring(9), "HHmmss")
                        // format is yyyy/MM/ddHH:mm:ss, yyyy-MM-ddHH:mm:ss
                        length == 18 -> computeToTime(dateOrTime.substring(10), "HHmmss", true)
                        // date format is yyyy/MM/dd, yyyy-MM-dd
                        // time format is HH:mm:ss
                        length > 18 -> computeToTime(dateOrTime.substring(11, 19), "HHmmss", true)
                        else -> throw RuntimeException(throws())
                    }
                }
                else -> throw RuntimeException(throws())
            }
        }

        private fun computeToDateTime(
            dateOrTime: String,
            pattern: String,
            removeIrrelevantChars: Boolean = false
        ): LocalDateTime {
            return if (removeIrrelevantChars) {
                LocalDateTime.parse(
                    removeIrrelevantCharsFromDateString(dateOrTime),
                    DateTimeFormatter.ofPattern(pattern)
                )
            } else {
                LocalDateTime.parse(dateOrTime, DateTimeFormatter.ofPattern(pattern))
            }
        }

        fun computeToDateTime(dateOrTime: Any?, throws: () -> String): LocalDateTime? {
            return when (dateOrTime) {
                null -> null
                is Date -> dateOrTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                is LocalDate -> dateOrTime.atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime()
                is LocalDateTime -> dateOrTime
                is String -> {
                    val str = removeIrrelevantCharsFromDateString(dateOrTime)
                    val length = str.length
                    when {
                        length >= 14 -> computeToDateTime(str.substring(0, 14), "yyyyMMddHHmmss").withNano(0)
                        else -> throw RuntimeException(throws())
                    }
                }
                else -> throw RuntimeException(throws())
            }
        }

        fun computeToFullDateTime(dateOrTime: Any?, throws: () -> String): LocalDateTime? {
            return when (dateOrTime) {
                null -> null
                is Date -> dateOrTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                is LocalDate -> dateOrTime.atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime()
                is LocalDateTime -> dateOrTime
                is String -> {
                    val str = removeIrrelevantCharsFromDateString(dateOrTime)
                    val length = str.length
                    when {
                        length == 14 -> computeToDateTime(str, "yyyyMMddHHmmss").withNano(0)
                        length == 15 -> computeToDateTime(str, "yyyyMMddHHmmssS")
                        length == 16 -> computeToDateTime(str, "yyyyMMddHHmmssSS")
                        length >= 17 -> computeToDateTime(str.substring(0, 17), "yyyyMMddHHmmssSSS")
                        else -> throw RuntimeException(throws())
                    }
                }
                else -> throw RuntimeException(throws())
            }
        }

        fun computeToCollection(value: Any?): List<Any?> {
            return when (value) {
                null -> listOf()
                is Array<*> -> value.toList()
                is List<*> -> value
                is Collection<*> -> value.toList()
                is String -> value.split(",").map { it.trim() }
                else -> listOf(value)
            }
        }
    }
}