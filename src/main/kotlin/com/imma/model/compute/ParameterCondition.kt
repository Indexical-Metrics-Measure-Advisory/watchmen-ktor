package com.imma.model.compute

interface ParameterCondition

enum class ParameterExpressionOperator(val operator: String) {
    EMPTY("empty"),
    NOT_EMPTY("not-empty"),
    EQUALS("equals"),
    NOT_EQUALS("not-equals"),
    LESS("less"),
    LESS_EQUALS("less-equals"),
    MORE("more"),
    MORE_EQUALS("more-equals"),
    IN("in"),
    NOT_IN("not-in"),
}

open class ParameterExpression : ParameterCondition {
    open var left: Parameter = ConstantParameter()
    open var operator: ParameterExpressionOperator = ParameterExpressionOperator.EQUALS
    open var right: Parameter = ConstantParameter()
}

enum class ParameterJointType(val joint: String) {
    AND("and"),
    OR("or"),
}

open class ParameterJoint : ParameterCondition {
    open var jointType: ParameterJointType = ParameterJointType.AND
    open var filters: List<ParameterCondition> = mutableListOf()
}

