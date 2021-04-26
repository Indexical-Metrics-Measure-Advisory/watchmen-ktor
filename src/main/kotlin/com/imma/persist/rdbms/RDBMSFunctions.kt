package com.imma.persist.rdbms

abstract class RDBMSFunctions {
	protected open fun asContinuousOperation(list: List<SQLPart>, operation: String): SQLPart {
		val values = mutableListOf<Any?>()
		val statement = list.joinToString(operation) { part ->
			values.addAll(part.values)
			part.statement
		}
		return SQLPart("($statement)", values)
	}

	open fun add(list: List<SQLPart>): SQLPart {
		return asContinuousOperation(list, " + ")
	}

	open fun subtract(list: List<SQLPart>): SQLPart {
		return asContinuousOperation(list, " - ")
	}

	open fun multiply(list: List<SQLPart>): SQLPart {
		return asContinuousOperation(list, " * ")
	}

	open fun divide(list: List<SQLPart>): SQLPart {
		return asContinuousOperation(list, " / ")
	}

	open fun mod(first: SQLPart, second: SQLPart): SQLPart {
		return asContinuousOperation(listOf(first, second), " % ")
	}

	abstract fun year(one: SQLPart): SQLPart

	abstract fun halfYear(one: SQLPart): SQLPart

	abstract fun quarter(one: SQLPart): SQLPart

	abstract fun month(one: SQLPart): SQLPart

	abstract fun weekOfYear(one: SQLPart): SQLPart

	abstract fun weekOfMonth(one: SQLPart): SQLPart

	abstract fun dayOfMonth(one: SQLPart): SQLPart

	abstract fun dayOfWeek(one: SQLPart): SQLPart

	abstract fun isEmpty(one: SQLPart): SQLPart

	abstract fun isNotEmpty(one: SQLPart): SQLPart

	open fun eq(one: SQLPart, another: SQLPart): SQLPart {
		return asContinuousOperation(listOf(one, another), " = ")
	}

	open fun notEq(one: SQLPart, another: SQLPart): SQLPart {
		return asContinuousOperation(listOf(one, another), " != ")
	}

	open fun lt(one: SQLPart, another: SQLPart): SQLPart {
		return asContinuousOperation(listOf(one, another), " < ")
	}

	open fun lte(one: SQLPart, another: SQLPart): SQLPart {
		return asContinuousOperation(listOf(one, another), " <= ")
	}

	open fun gt(one: SQLPart, another: SQLPart): SQLPart {
		return asContinuousOperation(listOf(one, another), " > ")
	}

	open fun gte(one: SQLPart, another: SQLPart): SQLPart {
		return asContinuousOperation(listOf(one, another), " >= ")
	}

	open fun exists(one: SQLPart, another: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		values.addAll(one.values)
		values.addAll(another.values)
		return SQLPart("${one.statement} IN (${another.statement})", values)
	}

	open fun notExists(one: SQLPart, another: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		values.addAll(one.values)
		values.addAll(another.values)
		return SQLPart("${one.statement} NOT IN (${another.statement})", values)
	}

	open fun hasText(one: SQLPart, another: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		values.addAll(one.values)
		values.addAll(another.values)
		return SQLPart("${one.statement} LIKE ('%' + ${another.statement} + '%')", values)
	}

	abstract fun hasOne(one: SQLPart, another: SQLPart): SQLPart

	abstract fun pull(fieldName: String, value: Any?): SQLPart

	abstract fun push(fieldName: String, value: Any?): SQLPart
}