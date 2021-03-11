package com.imma.model

open class Rect {
    open var x: Int = 0
    open var y: Int = 0
    open var width: Int = 0
    open var height: Int = 0
}

data class ReportRect(
    override var x: Int = 0,
    override var y: Int = 0,
    override var width: Int = 0,
    override var height: Int = 0
) : Rect() {}