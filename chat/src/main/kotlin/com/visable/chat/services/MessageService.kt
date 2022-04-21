package com.visable.chat.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.visable.chat.controllers.dtos.MessageDto
import com.visable.chat.data.Conversation
import com.visable.chat.data.ConversationMessages
import com.visable.chat.data.ConversationUsers
import com.visable.chat.data.Message
import com.visable.chat.repositories.ConversationRepository
import com.visable.chat.services.exceptions.InvalidRecipientException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service

@Service
class MessageService(
    val conversationRepository: ConversationRepository,
    val rabbitTemplate: RabbitTemplate,
    val objectMapper: ObjectMapper
) {
    fun sendMessage(message: MessageDto) {
        if (message.from == message.to) throw InvalidRecipientException()
        rabbitTemplate.convertAndSend("rabbit_exchange", "routing_key", message)
    }

    fun retrieveSentMessages(userId: Long): List<MessageDto> {
        return conversationRepository.findConversationsByUserId(userId.toString())
            .flatMap { it.messages.messages }
            .filter { it.from == userId }
            .sortedBy { it.timestamp }
            .map { it.toDto() }
    }

    fun retrieveReceivedMessages(userId: Long, from: Long = -1): List<MessageDto> {
        return conversationRepository.findConversationsByUserId(userId.toString())
            .flatMap { it.messages.messages }
            .filter { if (from == -1L) it.to == userId else it.to == userId && it.from == from }
            .sortedBy { it.timestamp }
            .map { it.toDto() }
    }

    @RabbitListener(queues = ["rabbit_queue"])
    fun consumeQueueMessage(messageDto: MessageDto) {
        val userIds = listOf(messageDto.from, messageDto.to)
        try { // Append message to existing conversation
            val conversationId =
                conversationRepository.findConversationIdByUserIds(userIds.joinToString(prefix = "[", postfix = "]"))
            conversationRepository.appendMessageToConversation(
                conversationId,
                objectMapper.writeValueAsString(Message.fromDto(messageDto))
            )
        } catch (e: EmptyResultDataAccessException) { // Create new conversation
            val conversation =
                Conversation(
                    -1,
                    ConversationUsers(userIds),
                    ConversationMessages(listOf(Message.fromDto(messageDto)))
                )
            conversationRepository.save(conversation)
        }
    }
}
