package com.imma.service.core.parameter

import com.imma.model.core.Factor
import com.imma.model.core.Topic
import com.imma.model.core.compute.*
import com.imma.service.core.PipelineTopics
import com.imma.service.core.PipelineTriggerData
import com.imma.utils.nothing
import java.math.BigDecimal
import java.time.LocalDate

@Suppress("EnumEntryName")
enum class ParameterShouldBe {
	any,
	numeric,
	date,
	collection
}

data class FoundFactor(val topic: Topic, val factor: Factor)

class ParameterKits {
	companion object {
		private fun checkMinSubParameterCount(parameter: ComputedParameter, count: Int) {
			val size = parameter.parameters.size
			if (size < count) {
				throw RuntimeException("At least $count sub parameter(s) in [$parameter], but only [$size] now.")
			}
		}

		private fun checkMaxSubParameterCount(parameter: ComputedParameter, count: Int) {
			val size = parameter.parameters.size
			if (size > count) {
				throw RuntimeException("At most $count sub parameter(s) in [$parameter], but [$size] now.")
			}
		}

		fun checkSubParameters(parameter: ComputedParameter) {
			when (parameter.type) {
				ParameterComputeType.none -> nothing()
				ParameterComputeType.add -> checkMinSubParameterCount(parameter, 2)
				ParameterComputeType.subtract -> checkMinSubParameterCount(parameter, 2)
				ParameterComputeType.multiply -> checkMinSubParameterCount(parameter, 2)
				ParameterComputeType.divide -> checkMinSubParameterCount(parameter, 2)
				ParameterComputeType.modulus -> {
					checkMinSubParameterCount(parameter, 2)
					checkMaxSubParameterCount(parameter, 2)
				}
				ParameterComputeType.`year-of` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`half-year-of` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`quarter-of` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`month-of` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`week-of-year` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`week-of-month` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`day-of-month` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`day-of-week` -> checkMaxSubParameterCount(parameter, 1)
				ParameterComputeType.`case-then` -> {
					checkMinSubParameterCount(parameter, 1)
					if (parameter.parameters.count { it.on == null } > 1) {
						throw RuntimeException("Multiple anyway routes in case-then expression of [$parameter] is not allowed.")
					}
				}
			}
		}

		fun checkShouldBe(parameter: ComputedParameter, shouldBe: ParameterShouldBe) {
			val type = parameter.type

			when {
				type == ParameterComputeType.none -> nothing()
				type == ParameterComputeType.`case-then` -> nothing()
				shouldBe == ParameterShouldBe.any -> nothing()
				// cannot get date by computing except case-then
				shouldBe == ParameterShouldBe.date -> throw RuntimeException("Cannot get date result on parameter[$parameter].")
				shouldBe == ParameterShouldBe.numeric -> nothing()
				// cannot get collection by computing except case-then
				shouldBe == ParameterShouldBe.collection -> throw RuntimeException("Cannot get collection result on parameter[$parameter].")
			}
		}

		fun readTopicFactorParameter(
			parameter: TopicFactorParameter,
			topics: PipelineTopics,
			validOrThrow: (topicId: String) -> Unit
		): FoundFactor {
			val topicId = parameter.topicId
			if (topicId.isBlank()) {
				throw RuntimeException("Topic id of parameter[$parameter] cannot be blank.")
			}
			validOrThrow(topicId)
			val topic = topics[topicId]
				?: throw RuntimeException("Topic[$topicId] of parameter[$parameter] not found.")

			val factorId = parameter.factorId
			if (factorId.isBlank()) {
				throw RuntimeException("Factor id of parameter[$parameter] cannot be blank.")
			}
			val factor = topic.factors.find { it.factorId == factorId }
				?: throw RuntimeException("Factor[$factorId] of parameter[$parameter] not found.")

			return FoundFactor(topic, factor)
		}

		fun getValueFromSourceData(factor: Factor, sourceData: PipelineTriggerData): Any? {
			val name = factor.name!!
			if (!name.contains('.')) {
				return sourceData[factor.name!!]
			} else {
				val parts = name.split('.')
				var source: Any? = sourceData
				for (part in parts) {
					when (source) {
						is Iterable<Any?> -> source = source.map { item ->
							when (item) {
								is Map<*, *> -> item[part]
								else -> throw RuntimeException("Cannot retrieve data from $source by [$part].")
							}
						}
						is Array<*> -> source = source.map { item ->
							when (item) {
								is Map<*, *> -> item[part]
								else -> throw RuntimeException("Cannot retrieve data from $source by [$part].")
							}
						}
						is Map<*, *> -> source = source[part]
						null -> nothing()
						else -> throw RuntimeException("Cannot retrieve data from $source by [$part].")
					}
				}
				return source
			}
		}


		fun computeToDate(date: Any?, parameter: Parameter): LocalDate? {
			return ValueKits.computeToDate(date) {
				"Cannot cast given value[$date] to date, which is computed by parameter[$parameter]."
			}
		}

		fun computeToNumeric(value: Any?, parameter: Parameter): BigDecimal? {
			return ValueKits.computeToNumeric(value) {
				"Cannot cast given value[$value] to numeric, which is computed by parameter[$parameter]."
			}
		}

		fun computeToCollection(value: Any?, parameter: Parameter): List<Any?> {
			return ValueKits.computeToCollection(value) {
				"Cannot cast given value to list[$value], which is computed by parameter[$parameter]."
			}
		}

		private fun computeConstant(
			statement: String,
			parameter: ConstantParameter,
			shouldBe: ParameterShouldBe,
			getValue: GetFirstValue
		): Any? {
			val v = ConstantParameterKits.computeConstant(statement, getValue) {
				"Cannot retrieve value of variable[$statement], which is defined by parameter[$parameter]."
			}
			return when (shouldBe) {
				ParameterShouldBe.any -> v
				ParameterShouldBe.numeric -> computeToNumeric(v, parameter)
				ParameterShouldBe.date -> computeToDate(v, parameter)
				ParameterShouldBe.collection -> computeToCollection(v, parameter)
			}
		}

		fun computeConstant(parameter: ConstantParameter, shouldBe: ParameterShouldBe, getValue: GetFirstValue): Any? {
			val statement = parameter.value

			return when {
				statement == null -> null
				statement.isEmpty() && shouldBe == ParameterShouldBe.any -> ""
				statement.isEmpty() -> null
				statement.isBlank() && shouldBe == ParameterShouldBe.any -> statement
				statement.isBlank() -> null
				else -> computeConstant(statement, parameter, shouldBe, getValue)
			}
		}
	}
}