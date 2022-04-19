package com.visable.chat.data

import org.hibernate.annotations.Type
import javax.persistence.*

@Entity(name = "conversations")
data class Conversation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    val users: ConversationUsers,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    val chat: ConversationMessages
)
