package com.imma.service.core.log

@Suppress("EnumEntryName")
enum class RunType(val type: String) {
	invalidate("invalidate"),
	disable("disable"),
	ignore("ignore"),

	start("start"),
	process("process"),
	fail("fail"),
	end("end"),

	`not-defined`("not-defined")
}