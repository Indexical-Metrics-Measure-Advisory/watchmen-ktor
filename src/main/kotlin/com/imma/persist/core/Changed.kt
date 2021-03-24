package com.imma.persist.core

class ColumnChanged(val column: Column) {
    private var oldValue: Any? = NotSet
    private var newValue: Any? = NotSet

    infix fun from(value: Any?) {
        this.oldValue = value
    }

    infix fun to(value: Any?) {
        this.newValue = value
    }
}

class Changed {
    val parts: MutableList<ColumnChanged> = mutableListOf()

    fun set(name: String): ColumnChanged {
        val changed = ColumnChanged(Column(name))
        parts.add(changed)
        return changed
    }
}