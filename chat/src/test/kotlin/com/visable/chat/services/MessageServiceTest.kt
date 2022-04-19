package com.visable.chat.services

import com.visable.chat.controllers.dtos.MessageDto
import com.visable.chat.data.Message
import com.visable.chat.repositories.ConversationRepository
import com.visable.chat.services.exceptions.InvalidRecipientException
import com.visable.chat.services.exceptions.UserNotFoundException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class MessageServiceTest {

    @MockK
    lateinit var conversationRepository: ConversationRepository

    @MockK
    lateinit var rabbitTemplate: RabbitTemplate

    @InjectMockKs
    lateinit var messageService: MessageService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun sendMessage_regularScenario_successful() {
        // given
        val messageDto = MessageDto(420, 6, "A beautiful message")
        // when
        messageService.sendMessage(messageDto)
        // then
        verify { rabbitTemplate.convertAndSend(any()) }
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
    fun sendMessage_nonExistingSender_userNotFoundException() {
        // given
        val messageDto = MessageDto(420, 6, "A beautiful message")
        // when
        assertThrows<UserNotFoundException> { messageService.sendMessage(messageDto) }
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

        every { conversationRepository.findSentMessages(420) }.returns(sentMessages())

        // when
        val result = messageService.retrieveSentMessages(420)
        // then
        verify { conversationRepository.findSentMessages(420) }
        Assertions.assertEquals(4, result.size)
        Assertions.assertEquals(expectedMessage0, result[0])
        Assertions.assertEquals(expectedMessage3, result[3])
    }

    @Test
    fun retrieveSentMessages_emptyResponse_successful() {
        // given
        every { conversationRepository.findSentMessages(420) }.returns(emptyList())
        // when
        val result = messageService.retrieveSentMessages(420)
        // then
        verify { conversationRepository.findSentMessages(420) }
        Assertions.assertTrue(result.isEmpty())
    }

    @Test
    fun retrieveReceivedMessages_allMessages_successful() {
        // given
        every { conversationRepository.findReceivedMessages(420) }.returns(receivedMessages())
        val expectedMessage0 = MessageDto(
            6, 420, "you used to get it in your fishnets",
            LocalDateTime.of(2022, 4, 1, 13, 30).toInstant(ZoneOffset.UTC)
        )
        val expectedMessage3 = MessageDto(
            6, 420, "landed in a very common crisis",
            LocalDateTime.of(2022, 4, 1, 13, 35).toInstant(ZoneOffset.UTC)
        )
        // when
        val result = messageService.retrieveReceivedMessages(420)
        // then
        verify { conversationRepository.findReceivedMessages(420) }
        Assertions.assertEquals(4, result.size)
        Assertions.assertEquals(expectedMessage0, result[0])
        Assertions.assertEquals(expectedMessage3, result[3])

    }

    @Test
    fun retrieveReceivedMessages_emptyResponse_successful() {
        // given
        every { conversationRepository.findReceivedMessages(420) }.returns(emptyList())
        // when
        val result = messageService.retrieveReceivedMessages(420)
        // then
        verify { conversationRepository.findReceivedMessages(420) }
        Assertions.assertTrue(result.isEmpty())
    }

    @Test
    fun retrieveReceivedMessages_fromUser_successful() {
        // given
        every { conversationRepository.findReceivedMessagesFrom(420, 6) }.returns(
            listOf(
                receivedMessages()[0],
                receivedMessages()[3]
            )
        )
        val expectedMessage0 = MessageDto(
            6, 420, "you used to get it in your fishnets",
            LocalDateTime.of(2022, 4, 1, 13, 30).toInstant(ZoneOffset.UTC)
        )
        val expectedMessage1 = MessageDto(
            6, 420, "landed in a very common crisis",
            LocalDateTime.of(2022, 4, 1, 13, 35).toInstant(ZoneOffset.UTC)
        )
        // when
        val result = messageService.retrieveReceivedMessages(420, 6)
        // then
        verify { conversationRepository.findReceivedMessages(420) }
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(expectedMessage0, result[0])
        Assertions.assertEquals(expectedMessage1, result[1])
    }

    @Test
    fun retrieveReceivedMessages_fromUserEmpty_successful() {
        // given
        every { conversationRepository.findReceivedMessagesFrom(420, 6) }.returns(emptyList())
        // when
        val result = messageService.retrieveReceivedMessages(420, 6)
        // then
        verify { conversationRepository.findReceivedMessagesFrom(420, 6) }
        Assertions.assertTrue(result.isEmpty())
    }

    private fun sentMessages(): List<Message> = listOf(
        Message(
            420,
            6,
            "Hello",
            LocalDateTime.parse("2022-04-01T13:30:00.000Z").toInstant(ZoneOffset.UTC)
        ),
        Message(
            420,
            7,
            "it's me",
            LocalDateTime.parse("2022-04-01T13:31:00.000Z").toInstant(ZoneOffset.UTC)
        ),
        Message(
            420,
            8,
            "I was wondering",
            LocalDateTime.parse("2022-04-01T13:32:00.000Z").toInstant(ZoneOffset.UTC)
        ),
        Message(
            420,
            6,
            "if after all these years you'd like to meet",
            LocalDateTime.parse("2022-04-01T13:33:00.000Z").toInstant(ZoneOffset.UTC)
        )
    )

    private fun receivedMessages(): List<Message> = listOf(
        Message(
            6,
            420,
            "you used to get it in your fishnets",
            LocalDateTime.parse("2022-04-01T13:30:00.000Z").toInstant(ZoneOffset.UTC)
        ),
        Message(
            7,
            420,
            "now you only get it in your night dress",
            LocalDateTime.parse("2022-04-01T13:31:00.000Z").toInstant(ZoneOffset.UTC)
        ),
        Message(
            8,
            420,
            "discarded all the naughty nights for niceness",
            LocalDateTime.parse("2022-04-01T13:32:00.000Z").toInstant(ZoneOffset.UTC)
        ),
        Message(
            6,
            420,
            "landed in a very common crisis",
            LocalDateTime.parse("2022-04-01T13:33:00.000Z").toInstant(ZoneOffset.UTC)
        )
    )
}