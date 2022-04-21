package com.visable.chat.data

import com.visable.chat.controllers.dtos.MessageDto
import java.time.Instant

data class Message(
    val from: Long,
    val to: Long,
    val content: String,
    val timestamp: Instant
) {
    companion object {
        fun fromDto(messageDto: MessageDto) =
            Message(messageDto.from, messageDto.to, messageDto.content, messageDto.timestamp)
    }

    fun toDto() = MessageDto(from, to, content, timestamp)
}
