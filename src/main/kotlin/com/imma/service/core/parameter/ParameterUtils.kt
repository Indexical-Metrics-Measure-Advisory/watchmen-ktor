package com.imma.service.core.parameter

import com.imma.model.compute.ComputedParameter
import com.imma.model.compute.ParameterComputeType
import org.jetbrains.kotlin.utils.doNothing

@Suppress("EnumEntryName")
enum class ParameterShouldBe {
    any,
    numeric,
    date
}

class ParameterUtils {
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
                ParameterComputeType.none -> doNothing()
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
                type == ParameterComputeType.none -> doNothing()
                type == ParameterComputeType.`case-then` -> doNothing()
                shouldBe == ParameterShouldBe.any -> doNothing()
                // cannot get date by computing except case-then
                shouldBe == ParameterShouldBe.date -> throw RuntimeException("Cannot get date result on parameter[$parameter].")
                shouldBe == ParameterShouldBe.numeric -> doNothing()
            }
        }
    }
}