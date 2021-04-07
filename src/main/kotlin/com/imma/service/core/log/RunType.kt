package com.imma.service.core.log

@Suppress("EnumEntryName")
enum class RunType(val type: String) {
    invalidate("invalidate"),
    disable("disable"),
    ignore("ignore"),
    fail("fail"),

    start("start"),
    end("end"),

    `not-defined`("not-defined")
}