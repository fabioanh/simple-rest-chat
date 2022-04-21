package com.visable.chat.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.visable.chat.controllers.dtos.MessageDto
import com.visable.chat.data.Conversation
import com.visable.chat.data.ConversationMessages
import com.visable.chat.data.ConversationUsers
import com.visable.chat.data.Message
import com.visable.chat.repositories.ConversationRepository
import com.visable.chat.services.exceptions.InvalidRecipientException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.dao.EmptyResultDataAccessException
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class MessageServiceTest {

    @MockK
    lateinit var conversationRepository: ConversationRepository

    @MockK
    lateinit var rabbitTemplate: RabbitTemplate

    @SpyK
    var objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @InjectMockKs
    lateinit var messageService: MessageService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun sendMessage_regularScenario_successful() {
        // given
        val messageDto = MessageDto(420, 6, "A beautiful message")
        justRun { rabbitTemplate.convertAndSend("rabbit_exchange", "routing_key", any<MessageDto>()) }
        // when
        messageService.sendMessage(messageDto)
        // then
        verify { rabbitTemplate.convertAndSend("rabbit_exchange", "routing_key", messageDto) }
    }

    @Test
    fun sendMessage_messageToSelf_invalidRecipientException() {
        // given
        val messageDto = MessageDto(420, 420, "A beautiful message")
        // when
        assertThrows<InvalidRecipientException> { messageService.sendMessage(messageDto) }
        // then
        // nothing
    }

    @Test
    fun retrieveSentMessages_regularScenario_successful() {
        // given
        val expectedMessage0 = MessageDto(
            420, 6, "Hello",
            LocalDateTime.of(2022, 4, 1, 13, 30)
                .toInstant(ZoneOffset.UTC)
        )
        val expectedMessage3 = MessageDto(
            420, 6, "if after all these years you'd like to meet",
            LocalDateTime.of(2022, 4, 1, 13, 33)
                .toInstant(ZoneOffset.UTC)
        )

        every { conversationRepository.findConversationsByUserId("420") }.returns(sampleConversations())

        // when
        val result = messageService.retrieveSentMessages(420)
        // then
        verify { conversationRepository.findConversationsByUserId("420") }
        Assertions.assertEquals(4, result.size)
        Assertions.assertEquals(expectedMessage0, result[0])
        Assertions.assertEquals(expectedMessage3, result[3])
    }

    @Test
    fun retrieveSentMessages_emptyResponse_successful() {
        // given
        every { conversationRepository.findConversationsByUserId("420") }.returns(emptyList())
        // when
        val result = messageService.retrieveSentMessages(420)
        // then
        verify { conversationRepository.findConversationsByUserId("420") }
        Assertions.assertTrue(result.isEmpty())
    }

    @Test
    fun retrieveReceivedMessages_allMessages_successful() {
        // given
        every { conversationRepository.findConversationsByUserId("420") }.returns(sampleConversations())
        val expectedMessage0 = MessageDto(
            6, 420, "you used to get it in your fishnets",
            LocalDateTime.of(2022, 4, 1, 13, 30).toInstant(ZoneOffset.UTC)
        )
        val expectedMessage3 = MessageDto(
            6, 420, "landed in a very common crisis",
            LocalDateTime.of(2022, 4, 1, 13, 33).toInstant(ZoneOffset.UTC)
        )
        // when
        val result = messageService.retrieveReceivedMessages(420)
        // then
        verify { conversationRepository.findConversationsByUserId("420") }
        Assertions.assertEquals(4, result.size)
        Assertions.assertEquals(expectedMessage0, result[0])
        Assertions.assertEquals(expectedMessage3, result[3])

    }

    @Test
    fun retrieveReceivedMessages_emptyResponse_successful() {
        // given
        every { conversationRepository.findConversationsByUserId("420") }.returns(emptyList())
        // when
        val result = messageService.retrieveReceivedMessages(420)
        // then
        verify { conversationRepository.findConversationsByUserId("420") }
        Assertions.assertTrue(result.isEmpty())
    }

    @Test
    fun retrieveReceivedMessages_fromUser_successful() {
        // given
        every { conversationRepository.findConversationsByUserId("420") }.returns(sampleConversations())
        val expectedMessage0 = MessageDto(
            6, 420, "you used to get it in your fishnets",
            LocalDateTime.of(2022, 4, 1, 13, 30).toInstant(ZoneOffset.UTC)
        )
        val expectedMessage1 = MessageDto(
            6, 420, "landed in a very common crisis",
            LocalDateTime.of(2022, 4, 1, 13, 33).toInstant(ZoneOffset.UTC)
        )
        // when
        val result = messageService.retrieveReceivedMessages(420, 6)
        // then
        verify { conversationRepository.findConversationsByUserId("420") }
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(expectedMessage0, result[0])
        Assertions.assertEquals(expectedMessage1, result[1])
    }

    @Test
    fun retrieveReceivedMessages_fromUserEmpty_successful() {
        // given
        every { conversationRepository.findConversationsByUserId("420") }.returns(emptyList())
        // when
        val result = messageService.retrieveReceivedMessages(420, 6)
        // then
        verify { conversationRepository.findConversationsByUserId("420") }
        Assertions.assertTrue(result.isEmpty())
    }

    @Test
    fun consumeQueueMessage_createNewConversation_successful() {
        // given
        every { conversationRepository.findConversationIdByUserIds(any()) }.throws(EmptyResultDataAccessException(1))
        every { conversationRepository.save(any()) }.returns(
            Conversation(
                1,
                ConversationUsers(listOf(420, 6)),
                ConversationMessages(
                    listOf(
                        Message(
                            420,
                            6,
                            "Hello",
                            LocalDateTime.parse("2022-04-01T13:30:00.000").toInstant(ZoneOffset.UTC)
                        )
                    )
                )
            )
        )
        val messageDto = MessageDto(
            420,
            6,
            "Hello",
            LocalDateTime.parse("2022-04-01T13:30:00.000").toInstant(ZoneOffset.UTC)
        )

        // when
        messageService.consumeQueueMessage(messageDto)

        // then
        verify { conversationRepository.findConversationIdByUserIds("[420, 6]") }
        verify {
            conversationRepository.save(
                Conversation(
                    -1,
                    ConversationUsers(listOf(420, 6)),
                    ConversationMessages(
                        listOf(
                            Message(
                                420,
                                6,
                                "Hello",
                                LocalDateTime.parse("2022-04-01T13:30:00.000").toInstant(ZoneOffset.UTC)
                            )
                        )
                    )
                )
            )
        }

    }

    @Test
    fun consumeQueueMessage_appendToConversation_successful() {
        // given
        every { conversationRepository.findConversationIdByUserIds(any()) }.returns(1)
        justRun { conversationRepository.appendMessageToConversation(1, any()) }
        val messageDto = MessageDto(
            6,
            420,
            "you used to get it in your fishnets",
            LocalDateTime.parse("2022-04-01T13:30:00.000").toInstant(ZoneOffset.UTC)
        )
        // when
        messageService.consumeQueueMessage(messageDto)
        // then
        verify { conversationRepository.findConversationIdByUserIds("[6, 420]") }
        verify { conversationRepository.appendMessageToConversation(1, any()) }
    }

    private fun sampleConversations() = listOf(
        Conversation(
            1,
            ConversationUsers(listOf(6, 420)),
            ConversationMessages(
                listOf(
                    sentMessages()[0],
                    sentMessages()[3],
                    receivedMessages()[0],
                    receivedMessages()[3]
                )
            )
        ),
        Conversation(
            2,
            ConversationUsers(listOf(7, 420)),
            ConversationMessages(
                listOf(
                    sentMessages()[1],
                    receivedMessages()[1],
                )
            )
        ),
        Conversation(
            3,
            ConversationUsers(listOf(8, 420)),
            ConversationMessages(
                listOf(
                    sentMessages()[2],
                    receivedMessages()[2],
                )
            )
        )
    )

    private fun sentMessages(): List<Message> = listOf(
        Message(
            420,
            6,
            "Hello",
            LocalDateTime.parse("2022-04-01T13:30:00.000").toInstant(ZoneOffset.UTC)
        ),
        Message(
            420,
            7,
            "it's me",
            LocalDateTime.parse("2022-04-01T13:31:00.000").toInstant(ZoneOffset.UTC)
        ),
        Message(
            420,
            8,
            "I was wondering",
            LocalDateTime.parse("2022-04-01T13:32:00.000").toInstant(ZoneOffset.UTC)
        ),
        Message(
            420,
            6,
            "if after all these years you'd like to meet",
            LocalDateTime.parse("2022-04-01T13:33:00.000").toInstant(ZoneOffset.UTC)
        )
    )

    private fun receivedMessages(): List<Message> = listOf(
        Message(
            6,
            420,
            "you used to get it in your fishnets",
            LocalDateTime.parse("2022-04-01T13:30:00.000").toInstant(ZoneOffset.UTC)
        ),
        Message(
            7,
            420,
            "now you only get it in your night dress",
            LocalDateTime.parse("2022-04-01T13:31:00.000").toInstant(ZoneOffset.UTC)
        ),
        Message(
            8,
            420,
            "discarded all the naughty nights for niceness",
            LocalDateTime.parse("2022-04-01T13:32:00.000").toInstant(ZoneOffset.UTC)
        ),
        Message(
            6,
            420,
            "landed in a very common crisis",
            LocalDateTime.parse("2022-04-01T13:33:00.000").toInstant(ZoneOffset.UTC)
        )
    )
}