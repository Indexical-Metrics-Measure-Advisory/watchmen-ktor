package com.imma.service.core

interface RunContext {
    fun isSourceTopic(topicId: String): Boolean
}