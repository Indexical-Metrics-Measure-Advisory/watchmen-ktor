package com.imma.persist.mango

import com.imma.model.core.*

class MongoFunctions {
	@Suppress("MemberVisibilityCanBePrivate", "unused")
	companion object {
		/**
		 * equals
		 */
		fun eq(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$eq" to listOf(a, b))
		}

		/**
		 * not equals
		 */
		fun notEq(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$ne" to listOf(a, b))
		}

		fun regex(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$regex" to listOf(a, b))
		}

		fun lt(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$lt" to listOf(a, b))
		}

		fun lte(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$lte" to listOf(a, b))
		}

		fun gt(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$gt" to listOf(a, b))
		}

		fun gte(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$gte" to listOf(a, b))
		}

		fun exists(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$in" to listOf(a, b))
		}

		fun notExists(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$nin" to listOf(a, b))
		}


		fun floor(a: Any?): Map<String, Any?> {
			return mapOf("\$floor" to a)
		}

		fun add(a: Any?, b: Any?, vararg rest: Any?): Map<String, List<Any?>> {
			return mapOf("\$add" to listOf(a, b, *rest))
		}

		fun add(values: List<Any?>): Map<String, List<Any?>> {
			return mapOf("\$add" to values)
		}

		fun subtract(a: Any, b: Any, vararg rest: Any): Map<String, List<Any>> {
			return mapOf("\$subtract" to listOf(a, b, *rest))
		}

		fun subtract(values: List<Any?>): Map<String, List<Any?>> {
			return mapOf("\$subtract" to values)
		}

		fun multiply(a: Any, b: Any, vararg rest: Any): Map<String, List<Any>> {
			return mapOf("\$multiply" to listOf(a, b, *rest))
		}

		fun multiply(values: List<Any?>): Map<String, List<Any?>> {
			return mapOf("\$multiply" to values)
		}

		fun divide(a: Any, b: Any, vararg rest: Any): Map<String, List<Any>> {
			return mapOf("\$divide" to listOf(a, b, *rest))
		}

		fun divide(values: List<Any?>): Map<String, List<Any?>> {
			return mapOf("\$divide" to values)
		}

		fun mod(a: Any?, b: Any?): Map<String, List<Any?>> {
			return mapOf("\$mod" to listOf(a, b))
		}

		fun year(a: Any?): Map<String, Any?> {
			return mapOf("\$year" to a)
		}

		/**
		 * compute month of year
		 * first half when less than or equals 6,
		 * second half when more than 6
		 */
		fun halfYear(a: Any?): Map<String, Any?> {
			//IMPORTANT be very careful here, they are not java/kotlin syntax.
			// they are MongoFunctions.if/CondIf.then/CondThen.else functions!
			return `if`(lte(month(a), 6)).then(HALF_YEAR_FIRST).`else`(HALF_YEAR_SECOND)

			//IMPORTANT following also works, likes native kotlin syntax very much!
			// return `if`(lessThanOrEqual(month(a), 6)) then HALF_YEAR_FIRST `else` HALF_YEAR_SECOND
		}

		fun quarter(a: Any?): Map<String, Any> {
			//IMPORTANT be very careful here, they are not java/kotlin syntax.
			// they are MongoFunctions.case/SwitchCase.then/SwitchThen.case/SwitchThen.default functions!
			val cases = case(lte("\$\$month", 3)).then(QUARTER_FIRST)
				.case(lte("\$\$month", 6)).then(QUARTER_SECOND)
				.case(lte("\$\$month", 9)).then(QUARTER_THIRD)
				.default(QUARTER_FOURTH)
			return vars("month" to month(a)).`do`(cases)

			//IMPORTANT following also works, likes native kotlin syntax very much!
			// val cases = (MF case lessThanOrEqual("\$\$month", 3) then QUARTER_FIRST
			//         case lessThanOrEqual("\$\$month", 6) then QUARTER_SECOND
			//         case lessThanOrEqual("\$\$month", 9) then QUARTER_THIRD
			//         default QUARTER_FOURTH)
			// return vars("month" to month(a)) `do` cases
		}

		fun month(a: Any?): Map<String, Any?> {
			return mapOf("\$month" to a)
		}

		fun weekOfYear(a: Any?): Map<String, Any?> {
			return mapOf("\$week" to a)
		}

		/**
		 * 1. store current date to variable
		 * 2. compute first day of current month
		 * 3. compute weekday of first ay of this month
		 *      1: Sunday ... 7: Saturday
		 *      first day of this month
		 * 4. subtract by 8 subtract, compute days left for first week, intermediate value
		 *      7 days: first day is Sunday
		 *      ...
		 *      1 day: first day is Saturday
		 * 5. mod 7, compute offset to first week (sunday is first day) of month. it is how many days in week 0, or 0 when it is not existed.
		 *      0: first week of month is 1, y/m/01 is sunday
		 *      1 - 6: first week of month is 0, and second week of month starts after x days
		 * 6. use current days subtract offset by step 5, get days left from week 0
		 *      note if current days is in week 0, result is negative value or zero
		 *      and subtract 1 additional, then
		 *          1. 1 ~ 7 -> 0 ~ 6, week 1
		 *          2. 8 ~ 14 -> 7 ~ 13, week 2
		 *          3. 15 ~ 21 -> 14 ~ 20, week 3
		 *          4. 22 ~ 28 -> 21 ~ 27, week 4
		 *          5. 29 ~ 31 -> 28 ~ 30, week 5
		 *          6. -6 ~ 0 -> ~ -7 ~ -1, week 0
		 * 7. divide 7, get week of month from zero, -1 ~ 4.x
		 * 8. floor, get integer value which is zero based, -1 ~ 4
		 * 9. add 1, get week of month which is 1 based, 0 ~ 5
		 */
		fun weekOfMonth(a: Any?): Map<String, Any> {
			// step 2, get first day of month
			val firstDay = date(MF.year("\$\$currentDay"), MF.month("\$\$currentDay"), 1)
			// step 3, get day of week of first day, 1 (Sunday) ~ 7 (Saturday)
			val firstDayOfWeek = dayOfWeek(firstDay)
			// step 4, get days left in week 0, 1 (Saturday) ~ 7 (Sunday)
			// step 5, get offset, 0 ~ 6
			val daysInWeek0 = mod(subtract(8, firstDayOfWeek), 7)
			// step 6.1, get current day of month
			// step 6.2, get days left after eliminate week 0 days
			// step 6.3, subtract 1
			val daysLeft = subtract(dayOfMonth("\$\$currentDay"), daysInWeek0, 1)
			// step 7, get zero based week of month
			// step 8, floor
			val weekOfMonthBasedOn0 = floor(divide(daysLeft, 7))
			// step 9, get 1 based week of month
			val weekOfMonth = add(weekOfMonthBasedOn0, 1)
			return vars("currentDay" to a).`do`(weekOfMonth)
		}

		fun dayOfMonth(a: Any?): Map<String, Any?> {
			return mapOf("\$dayOfMonth" to a)
		}

		fun dayOfWeek(a: Any?): Map<String, Any?> {
			return mapOf("\$dayOfWeek" to a)
		}

		fun date(year: Any, month: Any? = null, dayOfMonth: Any? = null): Map<String, Map<String, Any?>> {
			return mapOf(
				"\$dateFromParts" to mapOf(
					"year" to year,
					"month" to month,
					"day" to dayOfMonth
				)
			)
		}

		/**
		 * { $cond: { if: <boolean-expression>, then: <true-case>, else: <false-case> } }
		 * `if`().then().`else`()
		 */
		infix fun `if`(expression: Any?): CondIf {
			return CondIf(expression)
		}

		/**
		 * { $let: { vars: { <var1>: <expression>, ... }, in: <expression> } }
		 * vars().`do`()
		 */
		fun vars(variable: Pair<String, Any?>, vararg rest: Pair<String, Any?>): LetIn {
			return LetIn(mapOf(variable, *rest))
		}

		/**
		 * { $switch: { branches: [ { case: <expression>, then: <expression> }, { case: <expression>, then: <expression> }, ... ], default: <expression> } }
		 * 1. case().then().case().then()....`default`()
		 * 2. case().then().case().then()....`done`(), on no default case
		 */
		infix fun case(expression: Any?): SwitchCase {
			return SwitchCase(mutableListOf(), expression)
		}
	}
}

/**
 * { $switch: { branches: [ { case: <expression>, then: <expression> }, { case: <expression>, then: <expression> }, ... ], default: <expression> } }
 */
class SwitchCase(private val cases: MutableList<Map<String, Any?>>, private val caseExpression: Any?) {
	infix fun then(thenExpression: Any?): SwitchThen {
		val thisCase = mapOf("case" to caseExpression, "then" to thenExpression)
		cases += thisCase
		return SwitchThen(cases)
	}
}

/**
 * { $switch: { branches: [ { case: <expression>, then: <expression> }, { case: <expression>, then: <expression> }, ... ], default: <expression> } }
 */
class SwitchThen(private val cases: MutableList<Map<String, Any?>>) {
	infix fun case(caseExpression: Any?): SwitchCase {
		return SwitchCase(cases, caseExpression)
	}

	infix fun default(expression: Any?): Map<String, Map<String, Any?>> {
		return mapOf(
			"\$switch" to mapOf(
				"branches" to cases,
				"default" to expression
			)
		)
	}

	fun done(): Map<String, Map<String, Any?>> {
		return mapOf(
			"\$switch" to mapOf(
				"branches" to cases
			)
		)
	}
}

/**
 * { $let: { vars: { <var1>: <expression>, ... }, in: <expression> } }
 */
class LetIn(private val variables: Map<String, Any?>) {
	infix fun `do`(expression: Any?): Map<String, Map<String, Any?>> {
		return mapOf(
			"\$let" to mapOf(
				"vars" to variables,
				"in" to expression
			)
		)
	}
}

/**
 * { $cond: { if: <boolean-expression>, then: <true-case>, else: <false-case> } }
 */
class CondThen(private val expression: Any?, private val trueCase: Any?) {
	infix fun `else`(falseCase: Any?): Map<String, Map<String, Any?>> {
		return mapOf(
			"\$cond" to mapOf(
				"if" to expression,
				"then" to trueCase,
				"else" to falseCase
			)
		)
	}
}

/**
 * { $cond: { if: <boolean-expression>, then: <true-case>, else: <false-case> } }
 */
class CondIf(private val expression: Any?) {
	infix fun then(trueCase: Any?): CondThen {
		return CondThen(expression, trueCase)
	}
}

typealias MF = MongoFunctions
