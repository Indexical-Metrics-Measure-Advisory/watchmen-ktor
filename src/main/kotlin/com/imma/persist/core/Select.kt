package com.imma.persist.core

import com.imma.persist.core.build.SelectBuilder

enum class SelectColumnArithmetic {
	COUNT,
	SUM,
	AVG,
	MAX,
	MIN,
}

class SelectColumn(val element: Element) {
	var alias: String? = null
	var arithmetic: SelectColumnArithmetic? = null
}

class Select {
	val columns: MutableList<SelectColumn> = mutableListOf()
}

fun select(block: SelectBuilder.() -> Unit): Select {
	val select = Select()
	val builder = SelectBuilder(select)
	builder.block()
	return select
}
