package com.imma.model.compute

enum class ParameterKind(val kind: String) {
    topic("topic"),
    constant("constant"),
    computed("computed");
}

/**
 * parameter can be conditional, depends on where to use
 */
open class Parameter(
    var kind: ParameterKind,
    var conditional: Boolean? = null,
    var on: ParameterJointDelegate? = null
)

data class TopicFactorParameter(
    var topicId: String,
    var factorId: String,
) : Parameter(kind = ParameterKind.topic)

data class ConstantParameter(
    var value: String = ""
) : Parameter(kind = ParameterKind.constant)

enum class ParameterComputeType(val type: String) {
    none("none"),
    add("add"),
    subtract("subtract"),
    multiply("multiply"),
    divide("divide"),
    modulus("modulus"),
    `year-of`("year-of"),
    `half-year-of`("half-year-of"),
    `quarter-of`("quarter-of"),
    `month-of`("month-of"),
    `week-of-year`("week-of-year"),
    `week-of-month`("week-of-month"),
    `day-of-month`("day-of-month"),
    `day-of-week`("day-of-week"),
    `case-then`("case-then");
}

data class ComputedParameter(
    var type: ParameterComputeType = ParameterComputeType.none,
    var parameters: MutableList<Parameter> = mutableListOf()
) : Parameter(kind = ParameterKind.computed)

typealias ParameterDelegate = MutableMap<String, Any>

fun ParameterDelegate.takeOrThrow(): Parameter {
    var parameter: Parameter? = this.takeIfIsTopicFactor()
    if (parameter != null) {
        return parameter
    }
    parameter = this.takeIfIsConstant()
    if (parameter != null) {
        return parameter
    }
    parameter = this.takeIfIsCompute()
    if (parameter != null) {
        return parameter
    }
    throw RuntimeException("Parameter[kind=${this["kind"]}] cannot be determined.")
}

fun ParameterDelegate.takeIfIsTopicFactor(): TopicFactorParameter? {
    val kind = this["kind"]
    if (kind == ParameterKind.topic.kind) {
        return TopicFactorParameter(this["topicId"] as String, this["factorId"] as String)
    } else {
        return null
    }
}

fun ParameterDelegate.takeIfIsConstant(): ConstantParameter? {
    val kind = this["kind"]
    if (kind == ParameterKind.topic.kind) {
        return ConstantParameter(this["value"] as String)
    } else {
        return null
    }
}

fun ParameterDelegate.takeIfIsCompute(): ComputedParameter? {
    val kind = this["kind"]
    if (kind == ParameterKind.computed.kind) {
        val type = ParameterComputeType.valueOf(this["type"] as String)

        @Suppress("UNCHECKED_CAST")
        val parameters = (this["parameters"] as List<ParameterDelegate>?)?.map { it.takeOrThrow() }?.toMutableList()
        return ComputedParameter(type, parameters ?: mutableListOf<Parameter>())
    } else {
        return null
    }
}
