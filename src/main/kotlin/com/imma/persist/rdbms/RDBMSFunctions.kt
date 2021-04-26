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
}