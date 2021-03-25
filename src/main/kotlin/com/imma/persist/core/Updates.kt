package com.imma.persist.core

enum class ColumnUpdateType {
    SET,
    PULL,
    PUSH
}

class ColumnUpdate(val column: Column, val type: ColumnUpdateType) {
    var value: Any? = NotSet

    infix fun to(value: Any?) {
        this.value = value
    }
}

class PullFromArray(private val updates: Updates, private val value: Any) {
    infix fun from(name: String) {
        val change = ColumnUpdate(Column(name), ColumnUpdateType.PULL)
        change.value = value
        updates.parts.add(change)
    }
}

class PushIntoArray(private val updates: Updates, private val value: Any) {
    infix fun into(name: String) {
        val change = ColumnUpdate(Column(name), ColumnUpdateType.PUSH)
        change.value = value
        updates.parts.add(change)
    }
}

class Updates {
    val parts: MutableList<ColumnUpdate> = mutableListOf()

    fun set(name: String): ColumnUpdate {
        val change = ColumnUpdate(Column(name), ColumnUpdateType.SET)
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

fun update(block: Updates.() -> Unit): Updates {
    val changes = Updates()
    changes.block()
    return changes
}