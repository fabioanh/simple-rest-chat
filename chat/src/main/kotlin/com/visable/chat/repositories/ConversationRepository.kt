package com.visable.chat.repositories

import com.visable.chat.data.Conversation
import org.springframework.data.repository.CrudRepository

interface ConversationRepository : CrudRepository<Conversation, Long>
