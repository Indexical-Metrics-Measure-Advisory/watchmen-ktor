package com.imma.persist.mysql

import com.imma.persist.rdbms.RDBMSFunctions
import com.imma.persist.rdbms.SQLPart

/**
 * The JSON functions were added in MySQL 5.7.8.
 * See https://dev.mysql.com/doc/refman/5.7/en/json-functions.html
 */
class MySQLFunctions : RDBMSFunctions() {
	override fun year(one: SQLPart): SQLPart {
		return SQLPart("YEAR(${one.statement})", one.values)
	}

	override fun halfYear(one: SQLPart): SQLPart {
		return SQLPart("CEIL(QUARTER(${one.statement}) / 2)", one.values)
	}

	override fun quarter(one: SQLPart): SQLPart {
		return SQLPart("QUARTER(${one.statement})", one.values)
	}

	override fun month(one: SQLPart): SQLPart {
		return SQLPart("MONTH(${one.statement})", one.values)
	}

	override fun weekOfYear(one: SQLPart): SQLPart {
		return SQLPart("WEEK(${one.statement})", one.values)
	}

	override fun weekOfMonth(one: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		repeat(5) { values.addAll(one.values) }
		return SQLPart(
			"IF(DAYOFWEEK(${one.statement}) <> 1, WEEK(${one.statement}) - WEEK(DATE_FORMAT(${one.statement}, '%Y-%m-01')), WEEK(${one.statement}) - WEEK(DATE_FORMAT(${one.statement}, '%Y-%m-01')) + 1)",
			values
		)
	}

	override fun dayOfMonth(one: SQLPart): SQLPart {
		return SQLPart("DAYOFMONTH(${one.statement})", one.values)
	}

	override fun dayOfWeek(one: SQLPart): SQLPart {
		return SQLPart("DAYOFWEEK(${one.statement})", one.values)
	}

	override fun isEmpty(one: SQLPart): SQLPart {
		return SQLPart("IF(${one.statement} IS NULL OR '', TRUE, FALSE)", one.values)
	}

	override fun isNotEmpty(one: SQLPart): SQLPart {
		return SQLPart("IF(${one.statement} IS NULL OR '', FALSE, TRUE)", one.values)
	}

	/**
	 * IMPORTANT only string value can be proceed correctly
	 */
	override fun hasOne(one: SQLPart, another: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		repeat(2) { values.addAll(one.values) }
		values.addAll(another.values)
		return SQLPart(
			"IF(${one.statement} IS NULL OR '', FALSE, IF(JSON_CONTAINS(${one.statement}, CAST(${another.statement} AS JSON)) = 0, FALSE, TRUE))",
			values
		)
	}

	/**
	 * IMPORTANT only string value can be proceed correctly
	 */
	override fun pull(fieldName: String, value: Any?): SQLPart {
		return SQLPart(
			"$fieldName = IF($fieldName IS NULL OR '', $fieldName, JSON_REMOVE($fieldName, JSON_UNQUOTE(JSON_SEARCH($fieldName, 'one', ?))))",
			listOf(value, value)
		)
	}

	/**
	 * IMPORTANT only string value can be proceed correctly
	 */
	override fun push(fieldName: String, value: Any?): SQLPart {
		return SQLPart(
			"$fieldName = IF($fieldName IS NULL OR '', CAST(CONCAT('[', ?, ']') AS JSON), JSON_ARRAY_APPEND($fieldName, '$', ?))",
			listOf(value, value)
		)
	}
}