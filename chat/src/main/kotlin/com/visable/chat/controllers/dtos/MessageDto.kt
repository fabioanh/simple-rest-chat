package com.visable.chat.controllers.dtos

import java.time.Instant

data class MessageDto(
    var from: Long,
    val to: Long,
    val content: String,
    val timestamp: Instant = Instant.now()
)
