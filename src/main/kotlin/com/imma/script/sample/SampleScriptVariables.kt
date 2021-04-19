package com.imma.script.sample

import kotlin.script.templates.standard.ScriptTemplateWithBindings

@Suppress("UNUSED_PARAMETER", "unused")
var ScriptTemplateWithBindings.sampleVariable: Int
    get() = bindings["sampleVariable"] as Int
    set(value) {
        throw UnsupportedOperationException()
    }
