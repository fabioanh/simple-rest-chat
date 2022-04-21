package com.visable.chat.repositories

import com.visable.chat.data.Conversation
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface ConversationRepository : CrudRepository<Conversation, Long> {
    /**
     * Find the conversation identifier given the list of user ids taking part in the conversation
     */
    @Query("select * from conversations WHERE (users->'users')\\:\\:jsonb @> :userId\\:\\:jsonb", nativeQuery = true)
    fun findConversationsByUserId(@Param("userId") userId: String): List<Conversation>

    /**
     * Find the conversation identifier given the list of user ids taking part in the conversation
     */
    @Query("select id from conversations WHERE (users->'users')\\:\\:jsonb @> :userIds\\:\\:jsonb", nativeQuery = true)
    fun findConversationIdByUserIds(@Param("userIds") userIds: String): Long

    /**
     * Appends a message to the conversation identified by the given id
     */
    @Modifying
    @Transactional
    @Query(
        "UPDATE conversations " +
                "SET messages = jsonb_set(" +
                "messages\\:\\:jsonb, " +
                "array['messages'], " +
                "(messages->'messages')\\:\\:jsonb || :message\\:\\:jsonb) WHERE id = :conversationId",
        nativeQuery = true
    )
    fun appendMessageToConversation(@Param("conversationId") conversationId: Long, @Param("message") message: String)
}
