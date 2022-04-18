package com.visable.chat.services

import com.visable.chat.controllers.dtos.MessageDto
import com.visable.chat.repositories.ConversationRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class MessageService(
    val conversationRepository: ConversationRepository,
    val rabbitTemplate: RabbitTemplate
) {
    fun sendMessage(message: MessageDto) {
        TODO("implement")
    }

    fun retrieveSentMessages(userId: Long): List<MessageDto> {
        TODO("implement")
    }

    fun retrieveReceivedMessages(userId: Long, from: Long = -1): List<MessageDto> {
        TODO("implement")
    }
}
