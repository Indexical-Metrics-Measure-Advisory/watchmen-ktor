package com.imma.service.core.parameter

import com.imma.model.compute.ParameterExpression
import com.imma.model.compute.ParameterExpressionOperator
import com.imma.model.compute.ParameterJoint
import com.imma.model.compute.ParameterJointType
import com.imma.model.core.Topic
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

/**
 * condition builder for workout a where
 * which means:
 * 1. topic/factor which should be kept must be given kept topic
 * 2. any topic in topic/factor parameter, if not (1), then must be source topic.
 * 3. any variable in constant parameter must be source topic or can be found from variables
 */
class ConditionBuilder(keptTopic: Topic) {
    private val parameterBuilder: ParameterBuilder = ParameterBuilder(keptTopic)

    private fun createWhere(jointType: ParameterJointType): Where {
        return if (jointType == ParameterJointType.or) Or() else And()
    }

    fun build(joint: ParameterJoint): Where {
        return this.build(createWhere(joint.jointType), joint)
    }

    fun build(where: Where, joint: ParameterJoint): Where {
        where.parts += joint.filters.map { filter ->
            when (filter) {
                is ParameterJoint -> this.build(joint)
                is ParameterExpression -> Expression().apply {
                    left = parameterBuilder.build(filter.left)
                    operator = toExpressionOperator(filter.operator)
                    right = filter.right?.let { parameterBuilder.build(it) }
                }
                else -> throw RuntimeException("Unsupported filter[$filter].")
            }
        }
        return where
    }
}