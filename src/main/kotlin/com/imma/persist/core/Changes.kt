package com.imma.persist.core

enum class ColumnChangeType {
    SET,
    PULL,
    PUSH
}

class ColumnChange(val column: Column, val type: ColumnChangeType) {
    var value: Any? = NotSet

    infix fun to(value: Any?) {
        this.value = value
    }
}

class PullFromArray(private val changes: Changes, private val value: Any) {
    infix fun from(name: String) {
        val change = ColumnChange(Column(name), ColumnChangeType.PULL)
        change.value = value
        changes.parts.add(change)
    }
}

class PushIntoArray(private val changes: Changes, private val value: Any) {
    infix fun into(name: String) {
        val change = ColumnChange(Column(name), ColumnChangeType.PUSH)
        change.value = value
        changes.parts.add(change)
    }
}

class Changes {
    val parts: MutableList<ColumnChange> = mutableListOf()

    fun set(name: String): ColumnChange {
        val change = ColumnChange(Column(name), ColumnChangeType.SET)
        parts.add(change)
        return change
    }

    fun pull(vararg value: Any): PullFromArray {
        return PullFromArray(this, value)
    }

    fun push(vararg value: Any): PushIntoArray {
        return PushIntoArray(this, value)
    }
}

fun change(block: Changes.() -> Unit): Changes {
    val changes = Changes()
    changes.block()
    return changes
}