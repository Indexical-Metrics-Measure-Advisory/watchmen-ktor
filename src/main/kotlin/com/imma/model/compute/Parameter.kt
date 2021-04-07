package com.imma.model.compute

@Suppress("EnumEntryName")
enum class ParameterKind(val kind: String) {
    topic("topic"),
    constant("constant"),
    computed("computed");
}

/**
 * parameter can be conditional, depends on where to use
 */
open class Parameter(
    val kind: ParameterKind,
    open val conditional: Boolean? = null,
    open val on: ParameterJoint? = null
)

data class TopicFactorParameter(
    val topicId: String = "",
    val factorId: String = "",
    override val conditional: Boolean? = null,
    override val on: ParameterJoint? = null
) : Parameter(kind = ParameterKind.topic)

data class ConstantParameter(
    val value: String? = null,
    override val conditional: Boolean? = null,
    override val on: ParameterJoint? = null
) : Parameter(kind = ParameterKind.constant)

@Suppress("EnumEntryName")
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
    val type: ParameterComputeType = ParameterComputeType.none,
    val parameters: MutableList<Parameter> = mutableListOf(),
    override val conditional: Boolean? = null,
    override val on: ParameterJoint? = null
) : Parameter(kind = ParameterKind.computed)

typealias ParameterDelegate = MutableMap<String, Any>

fun takeAsParameterOrThrow(map: Map<*, *>): Parameter {
    return takeIfIsTopicFactor(map)
        ?: takeIfIsConstant(map)
        ?: takeIfIsCompute(map)
        ?: throw RuntimeException("Unsupported parameter with[kind=${map["kind"]}].")
}

private fun takeIfConditional(map: Map<*, *>): Pair<Boolean, ParameterJoint?> {
    val conditional = map["conditional"]
    val on = map["on"]
    return when {
        conditional == false -> Pair(false, null)
        conditional != null && !conditional.toString().toBoolean() -> Pair(false, null)
        // ignore when condition not defined
        on == null -> Pair(false, null)
        on !is Map<*, *> -> throw RuntimeException("Condition should be a map, but is [$on] now.")
        else -> Pair(true, takeAsParameterJointOrThrow(on))
    }
}

private fun takeIfIsTopicFactor(map: Map<*, *>): TopicFactorParameter? {
    val kind = map["kind"]
    return if (kind == ParameterKind.topic.kind) {
        val (conditional, on: ParameterJoint?) = takeIfConditional(map)
        val topicId = map["topicId"]?.toString()
        if (topicId.isNullOrBlank()) {
            throw RuntimeException("Topic Id of topic/factor parameter cannot be null or blank.")
        }
        val factorId = map["factorId"]?.toString()
        if (factorId.isNullOrBlank()) {
            throw RuntimeException("Factor Id of topic/factor parameter cannot be null or blank.")
        }

        TopicFactorParameter(topicId, factorId, conditional, on)
    } else {
        null
    }
}

private fun takeIfIsConstant(map: Map<*, *>): ConstantParameter? {
    val kind = map["kind"]
    return if (kind == ParameterKind.topic.kind) {
        val (conditional, on: ParameterJoint?) = takeIfConditional(map)
        ConstantParameter(map["value"]?.toString(), conditional, on)
    } else {
        null
    }
}

private fun takeAsParameters(parameters: Collection<*>): MutableList<Parameter> {
    return when {
        parameters.isEmpty() -> throw RuntimeException("Sub parameter of compute parameter cannot be empty.")
        parameters.any { sub -> sub !is Map<*, *> } -> throw RuntimeException("Every sub parameter of compute parameter should be a map.")
        else -> parameters.map { takeAsParameterOrThrow(it as Map<*, *>) }.toMutableList()
    }
}

private fun asComputeType(type: Any): ParameterComputeType? {
    return ParameterComputeType.values().find { it.type == type.toString() }
}

/**
 * convert map.
 * 1. sub parameters cannot be null
 * 2. sub parameters must be list or array
 * 3. sub parameters cannot be empty
 */
private fun takeIfIsCompute(map: Map<*, *>): ComputedParameter? {
    val kind = map["kind"]
    if (kind == ParameterKind.computed.kind) {
        val (conditional, on) = takeIfConditional(map)
        val computeType = when (val type = map["type"]) {
            null -> throw RuntimeException("Type of compute parameter cannot be null.")
            else -> asComputeType(type) ?: throw RuntimeException("Unsupported type[$type] of compute parameter.")
        }
        val params: MutableList<Parameter> = when (val parameters = map["parameters"]) {
            null -> throw RuntimeException("Sub parameter of compute parameter cannot be null.")
            is List<*> -> takeAsParameters(parameters)
            is Array<*> -> takeAsParameters(parameters.asList())
            else -> throw RuntimeException("Parameters of compute parameter should be a list or an array, but is [$parameters] now.")
        }

        return ComputedParameter(computeType, params, conditional, on)
    } else {
        return null
    }
}
