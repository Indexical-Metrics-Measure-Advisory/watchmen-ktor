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
    open var conditional: Boolean? = null,
    open var on: ParameterJoint? = null
)

data class TopicFactorParameter(
    var topicId: String? = "",
    var factorId: String? = "",
    override var conditional: Boolean? = null,
    override var on: ParameterJoint? = null
) : Parameter(kind = ParameterKind.topic)

data class ConstantParameter(
    var value: String? = "",
    override var conditional: Boolean? = null,
    override var on: ParameterJoint? = null
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
    var parameters: MutableList<Parameter> = mutableListOf(),
    override var conditional: Boolean? = null,
    override var on: ParameterJoint? = null
) : Parameter(kind = ParameterKind.computed)

typealias ParameterDelegate = MutableMap<String, Any>

fun ParameterDelegate.takeAsParameterOrThrow(): Parameter {
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

private fun ParameterDelegate.takeIfConditional(): Pair<Boolean, ParameterJoint?> {
    val conditional = this["conditional"]
    val on = this["on"]
    return when {
        conditional == false -> Pair(false, null)
        conditional != null && !conditional.toString().toBoolean() -> Pair(false, null)
        // ignore when condition not defined
        on == null -> Pair(false, null)
        on !is Map<*, *> -> throw RuntimeException("Unsupported condition[$on].")
        else -> Pair(true, on.takeAsParameterJointOrThrow())
    }
}

private fun ParameterDelegate.takeIfIsTopicFactor(): TopicFactorParameter? {
    val kind = this["kind"]
    if (kind == ParameterKind.topic.kind) {
        val (conditional, on: ParameterJoint?) = takeIfConditional()
        return TopicFactorParameter(
            conditional = conditional,
            on = on,
            topicId = this["topicId"] as String?,
            factorId = this["factorId"] as String?
        )
    } else {
        return null
    }
}

private fun ParameterDelegate.takeIfIsConstant(): ConstantParameter? {
    val kind = this["kind"]
    if (kind == ParameterKind.topic.kind) {
        val (conditional, on: ParameterJoint?) = takeIfConditional()
        return ConstantParameter(
            conditional = conditional,
            on = on,
            value = this["value"] as String?
        )
    } else {
        return null
    }
}

private fun ParameterDelegate.takeIfIsCompute(): ComputedParameter? {
    val kind = this["kind"]
    if (kind == ParameterKind.computed.kind) {
        val (conditional, on: ParameterJoint?) = takeIfConditional()
        val type = ParameterComputeType.valueOf(this["type"] as String)

        @Suppress("UNCHECKED_CAST")
        val parameters = (this["parameters"] as List<ParameterDelegate>?)?.map {
            it.takeAsParameterOrThrow()
        }?.toMutableList()
        return ComputedParameter(
            conditional = conditional,
            on = on,
            type = type,
            parameters = parameters ?: mutableListOf()
        )
    } else {
        return null
    }
}
