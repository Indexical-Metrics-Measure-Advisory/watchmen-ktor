package com.imma.persist.core

import com.imma.persist.core.build.ElementBuilder

enum class FactorUpdateType {
    SET,
    PULL,
    PUSH
}

class FactorUpdate(val factor: FactorElement, val type: FactorUpdateType) {
    var value: Any? = NotSet

    infix fun to(value: Any?) {
        this.value = value
    }
}

class PullFromArray(private val updates: Updates, private val value: Any) {
    infix fun from(factorName: String) {
        val change = FactorUpdate(ElementBuilder.SINGLETON.factor(factorName), FactorUpdateType.PULL)
        change.value = value
        updates.parts.add(change)
    }
}

class PushIntoArray(private val updates: Updates, private val value: Any) {
    infix fun into(factorName: String) {
        val change = FactorUpdate(ElementBuilder.SINGLETON.factor(factorName), FactorUpdateType.PUSH)
        change.value = value
        updates.parts.add(change)
    }
}

class Updates {
    val parts: MutableList<FactorUpdate> = mutableListOf()

    fun set(factorIdOrName: String): FactorUpdate {
        val change = FactorUpdate(ElementBuilder.SINGLETON.factor(factorIdOrName), FactorUpdateType.SET)
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
    return Updates().apply(block)
}