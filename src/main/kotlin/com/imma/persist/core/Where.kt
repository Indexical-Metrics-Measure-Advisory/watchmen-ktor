package com.imma.persist.core

interface Expression

enum class ColumnExpressionOperator {
    EQUALS,
    IN,
    INCLUDE,    // array value includes given value
    REGEXP      // ignore case
}

class ColumnExpression(val column: Column) : Expression {
    private var operator: Any? = NotSet
    private var value: Any? = NotSet

    infix fun eq(value: Any?) {
        this.operator = ColumnExpressionOperator.EQUALS
        this.value = value
    }

    infix fun `in`(values: List<Any>) {
        this.operator = ColumnExpressionOperator.IN
        this.value = values
    }

    infix fun include(value: Any) {
        this.operator = ColumnExpressionOperator.INCLUDE
        this.value = value
    }

    infix fun regex(pattern: String) {
        this.operator = ColumnExpressionOperator.REGEXP
        this.value = pattern
    }
}

open class Segments : Expression {
    val parts: MutableList<Expression> = mutableListOf()

    fun column(name: String): ColumnExpression {
        val expression = ColumnExpression(Column(name))
        parts.add(expression)
        return expression
    }
}

interface Where

open class And : Segments(), Where {
    fun or(block: Or.() -> Unit): And {
        parts.add(Criteria.or(block))
        return this
    }
}

class Or : Segments(), Where {
    fun and(block: And.() -> Unit): Or {
        parts.add(Criteria.and(block))
        return this
    }
}

class Criteria {
    companion object {
        fun and(block: And.() -> Unit): And {
            val where = And()
            where.block()
            return where
        }

        fun or(block: Or.() -> Unit): Or {
            val where = Or()
            where.block()
            return where
        }
    }
}

fun where(block: And.() -> Unit): And {
    return Criteria.and(block)
}
