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

open class ParameterExpression : ParameterCondition {
    open var left: Parameter = ConstantParameter()
    open var operator: ParameterExpressionOperator = ParameterExpressionOperator.equals
    open var right: Parameter = ConstantParameter()
}

enum class ParameterJointType(val joint: String) {
    and("and"),
    or("or");
}

open class ParameterJoint : ParameterCondition {
    open var jointType: ParameterJointType = ParameterJointType.and
    open var filters: List<ParameterCondition> = mutableListOf()
}

