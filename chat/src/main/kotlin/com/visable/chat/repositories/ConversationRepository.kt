package com.visable.chat.repositories

import com.visable.chat.data.Conversation
import com.visable.chat.data.Message
import org.springframework.data.repository.CrudRepository

interface ConversationRepository : CrudRepository<Conversation, Long> {
    /**
     * Find the messages sent by the userId given as parameter
     */
    fun findSentMessages(userId: Long): List<Message>

    /**
     * Find the messages sent to the userId given as parameter
     */
    fun findReceivedMessages(userId: Long): List<Message>

    /**
     * Find the messages sent to the userId from the user given as `from` parameter
     */
    fun findReceivedMessagesFrom(userId: Long, from: Long): List<Message>
}
