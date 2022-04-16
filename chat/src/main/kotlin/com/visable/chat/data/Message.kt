package com.visable.chat.data

import java.time.Instant

data class Message(
    val from: Long,
    val to: Long,
    val text: String,
    val timestamp: Instant
)
