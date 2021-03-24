package com.imma.persist.core

class Select {
    val parts: MutableList<String> = mutableListOf()

    fun include(name: String) {
        parts.add(name)
    }
}

fun select(block: Select.() -> Unit): Select {
    val fields = Select()
    fields.block()
    return fields
}
