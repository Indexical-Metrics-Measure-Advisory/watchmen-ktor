package com.imma.service.core.parameter

import com.imma.model.compute.ParameterExpression
import com.imma.model.compute.ParameterExpressionOperator
import com.imma.model.compute.ParameterJoint
import com.imma.model.compute.ParameterJointType
import com.imma.persist.core.*

private fun toExpressionOperator(operator: ParameterExpressionOperator?): ExpressionOperator? {
    return when (operator) {
        ParameterExpressionOperator.empty -> ExpressionOperator.empty
        ParameterExpressionOperator.`not-empty` -> ExpressionOperator.`not-empty`
        ParameterExpressionOperator.equals -> ExpressionOperator.equals
        ParameterExpressionOperator.`not-equals` -> ExpressionOperator.`not-equals`
        ParameterExpressionOperator.less -> ExpressionOperator.less
        ParameterExpressionOperator.`less-equals` -> ExpressionOperator.`less-equals`
        ParameterExpressionOperator.more -> ExpressionOperator.more
        ParameterExpressionOperator.`more-equals` -> ExpressionOperator.`more-equals`
        ParameterExpressionOperator.`in` -> ExpressionOperator.`in`
        ParameterExpressionOperator.`not-in` -> ExpressionOperator.`not-in`
        else -> null
    }
}

private fun Where.build(joint: ParameterJoint): Where {
    this.parts += joint.filters.map { filter ->
        when (filter) {
            is ParameterJoint -> ConditionBuilder.build(joint)
            is ParameterExpression -> Expression().apply {
                left = ParameterBuilder.build(filter.left)
                operator = toExpressionOperator(filter.operator)
                right = filter.right?.let { ParameterBuilder.build(it) }
            }
            else -> throw RuntimeException("Unsupported filter[$filter].")
        }
    }
    return this
}

class ConditionBuilder {
    companion object {
        fun build(joint: ParameterJoint): Where {
            return if (joint.jointType === ParameterJointType.or)
                Or().build(joint)
            else
                And().build(joint)
        }
    }
}