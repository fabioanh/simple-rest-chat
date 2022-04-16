package com.visable.chat.data

import java.time.Instant

data class Message(
    val from: Long,
    val to: Long,
    val content: String,
    val timestamp: Instant
)
