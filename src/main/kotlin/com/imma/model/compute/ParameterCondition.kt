package com.imma.model.compute

interface ParameterCondition

@Suppress("EnumEntryName")
enum class ParameterExpressionOperator(val operator: String) {
    empty("empty"),
    `not-empty`("not-empty"),
    equals("equals"),
    `not-equals`("not-equals"),
    less("less"),
    `less-equals`("less-equals"),
    more("more"),
    `more-equals`("more-equals"),
    `in`("in"),
    `not-in`("not-in");
}

data class ParameterExpression(
    var left: Parameter = ConstantParameter(),
    var operator: ParameterExpressionOperator = ParameterExpressionOperator.equals,
    var right: Parameter? = null
) : ParameterCondition

@Suppress("EnumEntryName")
enum class ParameterJointType(val joint: String) {
    and("and"),
    or("or");
}

data class ParameterJoint(
    var jointType: ParameterJointType = ParameterJointType.and,
    // ParamCondition List
    var filters: MutableList<ParameterCondition> = mutableListOf()
) : ParameterCondition

typealias ParameterJointDelegate = MutableMap<String, Any>

private fun takeOrThrow(map: Map<*, *>): ParameterCondition {
    return takeIfIsExpression(map)
        ?: takeIfIsJoint(map)
        ?: throw RuntimeException("Unsupported condition with[jointType=${map["jointType"]}, operator=${map["operator"]}].")
}

private fun ignoreRightPart(operator: ParameterExpressionOperator): Boolean {
    return ParameterExpressionOperator.empty === operator
            || ParameterExpressionOperator.`not-empty` === operator
}

private fun asOperator(operator: String): ParameterExpressionOperator? {
    return ParameterExpressionOperator.values().find { it.operator == operator }
}

/**
 * return expression object if it is, or return null if it isn't.
 * throw exception when it is and data incorrect
 */
private fun takeIfIsExpression(map: Map<*, *>): ParameterExpression? {
    val operator = map["operator"]?.toString()
    val op = operator?.run { asOperator(this) }
    val left = map["left"]
    val right = map["right"]
    return when {
        // not an expression, return null
        operator.isNullOrEmpty() -> null
        op == null -> throw RuntimeException("Unsupported expression operator[$operator] of expression.")
        left !is Map<*, *> -> throw RuntimeException("Left part of expression should be a map, but is [$left] now.")
        ignoreRightPart(op) -> ParameterExpression(
            takeAsParameterOrThrow(left),
            ParameterExpressionOperator.valueOf(operator)
        )
        right !is Map<*, *> -> throw RuntimeException("Right part of expression should be a map, but is [$left] now.")
        else -> ParameterExpression(
            takeAsParameterOrThrow(left),
            ParameterExpressionOperator.valueOf(operator),
            takeAsParameterOrThrow(right)
        )
    }
}

private fun takeAsFilters(filters: Collection<*>): MutableList<ParameterCondition> {
    return when {
        filters.isEmpty() -> throw RuntimeException("Sub filters of joint cannot be empty.")
        filters.any { sub -> sub !is Map<*, *> } -> throw RuntimeException("Every sub filter of joint should be a map.")
        else -> filters.map { takeOrThrow(it as Map<*, *>) }.toMutableList()
    }
}

private fun takeAsFilters(filters: Any?): MutableList<ParameterCondition> {
    return when (filters) {
        null -> throw RuntimeException("Sub filters of joint cannot be null.")
        is List<*> -> takeAsFilters(filters)
        is Array<*> -> takeAsFilters(filters.asList())
        else -> throw RuntimeException("Sub filters of joint should be a list or an array, but is [$filters] now.")
    }
}

private fun asJointType(type: String): ParameterJointType? {
    return ParameterJointType.values().find { it.joint == type }
}

private fun takeIfIsJoint(map: Map<*, *>): ParameterJoint? {
    val jointType = map["jointType"]?.toString()
    val jt = jointType?.run { asJointType(jointType) }

    return when {
        // not a joint, return null
        jointType.isNullOrEmpty() -> null
        jt == null -> throw RuntimeException("Unsupported joint type[$jointType] of joint.")
        else -> ParameterJoint(jt, takeAsFilters(map["filters"]))
    }
}

fun takeAsParameterJointOrThrow(map: Map<*, *>): ParameterJoint {
    val jointType = map["jointType"]?.toString()
    val joint: ParameterJointType = if (jointType.isNullOrEmpty()) {
        // use and when joint type is not defined
        ParameterJointType.and
    } else {
        asJointType(jointType) ?: throw RuntimeException("Unsupported joint type[$jointType] of joint.")
    }

    return ParameterJoint(joint, takeAsFilters(map["filters"]))
}
