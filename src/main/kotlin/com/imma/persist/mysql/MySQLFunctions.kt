package com.imma.persist.mysql

import com.imma.persist.rdbms.RDBMSFunctions
import com.imma.persist.rdbms.SQLPart

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
		return SQLPart("IF(${one.statement} IS NULL OR '', TRUE, FALSE", one.values)
	}

	override fun isNotEmpty(one: SQLPart): SQLPart {
		return SQLPart("IF(${one.statement} IS NULL OR '', FALSE, TRUE", one.values)
	}
}