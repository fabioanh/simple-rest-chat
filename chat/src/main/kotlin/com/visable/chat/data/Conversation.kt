package com.visable.chat.data

import javax.persistence.Entity

@Entity(name = "conversations")
data class Conversation(
    val id: Long,
    val users: ConversationUsers,
    val chat: ConversationMessages
)
