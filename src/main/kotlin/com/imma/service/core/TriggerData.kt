package com.imma.service.core

data class TriggerData(
    val topicId: String,
    val previous: Map<String, Any> = mapOf(),
    val now: Map<String, Any> = mapOf()
)