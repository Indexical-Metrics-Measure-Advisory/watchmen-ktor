package com.imma.service.core

data class TriggerData(
    val topicId: String,
    val previous: Any? = null,
    val now: Any? = null,
)