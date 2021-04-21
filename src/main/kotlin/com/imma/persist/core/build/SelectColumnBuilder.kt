package com.imma.persist.core.build

import com.imma.persist.core.SelectColumn
import com.imma.persist.core.SelectColumnArithmetic

class SelectColumnBuilder(private val column: SelectColumn) {
    /**
     * alias always at tail of column definition
     */
    infix fun alias(alias: String) {
        column.alias = alias
        // end, no return
    }

    private fun arithmetic(arithmetic: SelectColumnArithmetic): SelectColumnBuilder {
        column.arithmetic = arithmetic
        return this
    }

    fun count(): SelectColumnBuilder {
        return arithmetic(SelectColumnArithmetic.COUNT)
    }

    fun sum(): SelectColumnBuilder {
        return arithmetic(SelectColumnArithmetic.SUM)
    }

    fun avg(): SelectColumnBuilder {
        return arithmetic(SelectColumnArithmetic.AVG)
    }

    fun max(): SelectColumnBuilder {
        return arithmetic(SelectColumnArithmetic.MAX)
    }

    fun min(): SelectColumnBuilder {
        return arithmetic(SelectColumnArithmetic.MIN)
    }
}
