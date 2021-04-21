package com.imma.model.compute

import com.imma.model.snowflake.SnowflakeHelper

typealias GetFirstValue = (propertyName: String) -> Any?

class ConstantParameterKits {
	companion object {
		/**
		 * @param variable
		 *  x, x[.y[.z]...] -> property path, get value from given function, and get next from previous value, value must be a map. multiple segments are allowed,
		 *  nextSeq -> next auto generated sequence,
		 *  x.size, x[.y[.z]...].size -> property path, get value from given function, and size of this value. value must be a collection or a map,
		 *  x.length, x[.y[.z]...].length -> property path, get value from given function, and size of this value. value must be a string.
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
					part == "size" && value is Collection<*> -> value.size
					part == "size" && value is Map<*, *> -> value.size
					part == "length" && value is String -> value.length
					value is Map<*, Any?> -> value[part]
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