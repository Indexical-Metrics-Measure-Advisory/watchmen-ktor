package com.imma.model.compute

enum class ParameterKind(val kind: String) {
    TOPIC("topic"),
    CONSTANT("constant"),
    COMPUTED("computed")
}

open class Parameter(var kind: ParameterKind)

data class TopicFactorParameter(
    var topicId: String,
    var factorId: String,
) : Parameter(kind = ParameterKind.TOPIC)

data class ConstantParameter(
    var value: String = ""
) : Parameter(kind = ParameterKind.CONSTANT)

enum class ParameterComputeType(val type: String) {
    NONE("none"),
    ADD("add"),
    SUBTRACT("subtract"),
    MULTIPLY("multiply"),
    DIVIDE("divide"),
    MODULUS("modulus"),
    YEAR_OF("year-of"),
    HALF_YEAR_OF("half-year-of"),
    QUARTER_OF("quarter-of"),
    MONTH_OF("month-of"),
    WEEK_OF_YEAR("week-of-year"),
    WEEK_OF_MONTH("week-of-month"),
    DAY_OF_MONTH("day-of-month"),
    DAY_OF_WEEK("weekdays")
}

data class ComputedParameter(
    var type: ParameterComputeType = ParameterComputeType.NONE,
    var parameters: List<Parameter>
) : Parameter(kind = ParameterKind.COMPUTED)