package com.imma.persist.oracle

import com.imma.persist.rdbms.RDBMSFunctions
import com.imma.persist.rdbms.SQLPart

class OracleFunctions : RDBMSFunctions() {
	override fun year(one: SQLPart): SQLPart {
		return SQLPart("YEAR(${one.statement})", one.values)
	}

	override fun halfYear(one: SQLPart): SQLPart {
		return SQLPart("CASE WHEN MONTH(${one.statement}) <= 6 THEN 1 ELSE 2 END", one.values)
	}

	override fun quarter(one: SQLPart): SQLPart {
		return SQLPart("TO_NUMBER(TO_CHAR(${one.statement}, 'Q'))", one.values)
	}

	override fun month(one: SQLPart): SQLPart {
		return SQLPart("MONTH(${one.statement})", one.values)
	}

	/**
	 * 1. calculate first day of year: X = TRUNC(:DATE, 'Y')
	 * 2. calculate day of week: Y = TO_CHAR({X}, 'D'). sunday: 1 - saturday: 7
	 * 3. calculate days of zero week: Z = MOD(8 - {Y}, 7)
	 * 4. calculate days left, after days zero week is cut: A = TO_NUMBER(TO_CHAR(:DATE, 'DDD')) - {Z}
	 * 5. calculate week, -1 based: B = FLOOR(({A} - 1) / 7)
	 * 6. calculate final week, zero based: C = {B} + 1
	 */
	override fun weekOfYear(one: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		repeat(2) { values.addAll(one.values) }
		return SQLPart(
			"FLOOR((TO_NUMBER(TO_CHAR(${one.statement}, 'DDD')) - MOD(8 - TO_CHAR(TRUNC(${one.statement}, 'Y'), 'D'), 7) - 1) / 7) + 1",
			values
		)
	}

	/**
	 * 1. calculate first day of month: X = TRUNC(:DATE, 'MM')
	 * 2. calculate day of week: Y = TO_CHAR({X}, 'D'). sunday: 1 - saturday: 7
	 * 3. calculate days of zero week: Z = MOD(8 - {Y}, 7)
	 * 4. calculate days left, after days zero week is cut: A = TO_NUMBER(TO_CHAR(:DATE, 'DD')) - {Z}
	 * 5. calculate week, -1 based: B = FLOOR(({A} - 1) / 7)
	 * 6. calculate final week, zero based: C = {B} + 1
	 */
	override fun weekOfMonth(one: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		repeat(2) { values.addAll(one.values) }
		return SQLPart(
			"FLOOR((TO_NUMBER(TO_CHAR(${one.statement}, 'DD')) - MOD(8 - TO_CHAR(TRUNC(${one.statement}, 'MM'), 'D'), 7) - 1) / 7) + 1",
			values
		)
	}

	override fun dayOfMonth(one: SQLPart): SQLPart {
		return SQLPart("TO_NUMBER(TO_CHAR(${one.statement}), 'DD'))", one.values)
	}

	override fun dayOfWeek(one: SQLPart): SQLPart {
		return SQLPart("TO_NUMBER(TO_CHAR(${one.statement}, 'D'))", one.values)
	}

	override fun isEmpty(one: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		repeat(2) { values.addAll(one.values) }
		return SQLPart("CASE WHEN ${one.statement} IS NULL OR ${one.statement} = '' THEN TRUE ELSE FALSE END", values)
	}

	override fun isNotEmpty(one: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		repeat(2) { values.addAll(one.values) }
		return SQLPart("CASE WHEN ${one.statement} IS NULL OR ${one.statement} = '' THEN FALSE ELSE TRUE END", values)
	}

	/**
	 * IMPORTANT only string value can be proceed correctly
	 *
	 * @param another for given one, value is
	 */
	override fun hasOne(one: SQLPart, another: SQLPart): SQLPart {
		val values = mutableListOf<Any?>()
		repeat(3) { values.addAll(one.values) }
		return SQLPart(
			"CASE WHEN ${one.statement} IS NULL OR ${one.statement} = '' THEN FALSE WHEN JSON_EXISTS (${one.statement}, '\$[*]?(@ == \"${another.values[0]}\")') THEN TRUE ELSE FALSE END",
			values
		)
	}

	/**
	 * IMPORTANT only string value can be proceed correctly
	 */
	override fun pull(fieldName: String, value: Any?): SQLPart {
		val replacement = (value?.toString() ?: "").let { "(\"$it\")(,\\s)?" }
		return SQLPart("REGEXP_REPLACE($fieldName, '$replacement')", listOf())
	}

	/**
	 * IMPORTANT only string value can be proceed correctly
	 */
	override fun push(fieldName: String, value: Any?): SQLPart {
		val newValue = (value?.toString() ?: "")
		return SQLPart(
			"$fieldName = CASE WHEN $fieldName IS NULL OR $fieldName = '' THEN '[\"$newValue\"]' ELSE REPLACE($fieldName, ']', ',\"$newValue\"]') END",
			listOf(value, value)
		)
	}
}