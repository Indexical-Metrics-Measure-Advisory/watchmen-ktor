package com.imma.model.core.compute

import com.imma.model.snowflake.SnowflakeHelper

typealias GetFirstValue = (propertyName: String) -> Any?

class ConstantParameterKits {
	companion object {
		private fun getValueAsList(from: Any?, propertyName: String, throws: () -> String): List<Any?> {
			return when (from) {
				is Map<*, *> -> listOf(from[propertyName])
				is Collection<*> -> from.map { if (it is Map<*, *>) it[propertyName] else throw RuntimeException(throws()) }
				is Array<*> -> from.map { if (it is Map<*, *>) it[propertyName] else throw RuntimeException(throws()) }
				else -> listOf()
			}
		}

		/**
		 * variable can be:
		 * 1. x, x[.y[.z]...] -> property path, get value from given function, and get next from previous value, value must be a map. multiple segments are allowed,
		 * 2. nextSeq -> next auto generated sequence,
		 * 3. x.size, x[.y[.z]...].size -> property path, get value from given function, and size of this value. value must be a collection or a map,
		 * 4. x.length, x[.y[.z]...].length -> property path, get value from given function, and size of this value. value must be a string.
		 *
		 * if any path returns a collection or an array, the next level will retrieve value from each elements in this collection or array.
		 * IMPORTANT multiple collections or arrays are not supported yet.
		 *
		 * @param variable
		 * @param getFirstValue function to get value of first part of path. Ignored when it is {@code nextSeq}.
		 * @param throws generate an exception string when path is incorrect.
		 */
		private fun computeVariable(variable: String, getFirstValue: GetFirstValue, throws: () -> String): Any? {
			if (variable.isBlank()) {
				return null
			}

			var value: Any? = null
			val parts = variable.split(".")
			for ((index, part) in parts.withIndex()) {
				value = when {
					index == 0 && part == "nextSeq" -> SnowflakeHelper.nextSnowflakeId()
					index == 0 -> getFirstValue(part)
					value == null -> null
					part == "count" && value is Collection<*> -> value.size
					part == "count" && value is Map<*, *> -> value.size
					part == "length" && value is String -> value.length
					value is Map<*, *> -> value[part]
					value is Collection<*> -> mutableListOf(value.map { getValueAsList(it, part, throws) }.flatten())
					value is Array<*> -> mutableListOf(value.map { getValueAsList(it, part, throws) }.flatten())
					else -> throw RuntimeException(throws())
				}
			}

			return value
		}

		/**
		 * compute constant value.
		 *
		 * @param statement use {} to call variables, eg. {x}, {x.y}, {nextSeq}, {x.y.length}.
		 *  mixed also allowed, eg. a{x}, a{x}b{y.z}c.
		 */
		fun computeConstant(statement: String, getFirstValue: GetFirstValue, throws: () -> String): Any? {
			@Suppress("RegExpRedundantEscape")
			val regexp = "([^\\{]*(\\{[^\\}]+\\})?)".toRegex()
			val values = regexp.findAll(statement).map { segment ->
				val text = segment.value
				when (val braceStartIndex = text.indexOf("{")) {
					-1 -> text
					0 -> {
						val variable = text.substring(1, text.length - 1).trim()
						computeVariable(variable, getFirstValue, throws)
					}
					else -> {
						val prefix = text.substring(0, braceStartIndex)
						val variable = text.substring(1, text.length - 1).trim()
						"$prefix${computeVariable(variable, getFirstValue, throws) ?: ""}"
					}
				}
			}.toList()

			return if (values.size == 1) {
				values[0]
			} else {
				values.filterNotNull().joinToString("")
			}
		}
	}
}