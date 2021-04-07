package com.imma.model.compute

interface ParameterCondition

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
    var right: Parameter = ConstantParameter()
) : ParameterCondition

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

private fun takeOrThrow(map: Map<String, Any>): ParameterCondition {
    var condition: ParameterCondition? = takeIfIsExpression(map)
    if (condition != null) {
        return condition
    }
    condition = takeIfIsJoint(map)
    if (condition != null) {
        return condition
    }
    throw RuntimeException("Parameter Condition[jointType=${map["jointType"]}, operator=${map["operator"]}] cannot be determined.")
}

private fun takeIfIsExpression(map: Map<String, Any>): ParameterExpression? {
    val operator = map["operator"] as String?
    if (operator.isNullOrEmpty()) {
        return null
    } else {
        return ParameterExpression(
            @Suppress("UNCHECKED_CAST")
            (map["left"] as ParameterDelegate).takeAsParameterOrThrow(),
            ParameterExpressionOperator.valueOf(operator),
            @Suppress("UNCHECKED_CAST")
            (map["right"] as ParameterDelegate).takeAsParameterOrThrow(),
        )
    }
}

private fun takeIfIsJoint(map: Map<String, Any>): ParameterJoint? {
    val joint = map["jointType"] as String?
    if (joint.isNullOrEmpty()) {
        return null
    } else {
        return ParameterJoint(
            ParameterJointType.valueOf(joint),
            @Suppress("UNCHECKED_CAST")
            (map["filters"] as List<Map<String, Any>>?)?.map { takeOrThrow(it) }?.toMutableList() ?: mutableListOf(),
        )
    }
}

fun Map<*, *>.takeAsParameterJointOrThrow(): ParameterJoint {
    val jointType = this["jointType"]?.toString()
    val joint: ParameterJointType = if (jointType.isNullOrEmpty()) {
        ParameterJointType.and
    } else {
        ParameterJointType.valueOf(jointType)
    }

    @Suppress("UNCHECKED_CAST")
    val filters = (this["filters"] as List<Map<String, Any>>?)?.map { takeOrThrow(it) }?.toMutableList()
    return ParameterJoint(joint, filters ?: mutableListOf())
}
