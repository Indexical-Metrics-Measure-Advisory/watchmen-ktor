package com.imma.script.sample

import kotlin.script.templates.standard.ScriptTemplateWithBindings

var ScriptTemplateWithBindings.sampleVariable: Int
    get() = bindings["sampleVariable"] as Int
    set(value) {
        throw UnsupportedOperationException()
    }
