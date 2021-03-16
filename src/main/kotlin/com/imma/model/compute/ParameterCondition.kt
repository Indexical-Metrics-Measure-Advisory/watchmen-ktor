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
    var left: ParameterDelegate = mutableMapOf(),
    var operator: ParameterExpressionOperator = ParameterExpressionOperator.equals,
    var right: ParameterDelegate = mutableMapOf()
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