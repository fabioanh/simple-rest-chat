package com.visable.chat.controllers

import com.ninjasquad.springmockk.MockkBean
import com.visable.chat.controllers.dtos.MessageDto
import com.visable.chat.services.MessageService
import com.visable.chat.services.exceptions.InvalidRecipientException
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime
import java.time.ZoneOffset

@WebMvcTest(MessageController::class)
class MessageControllerTest {

    @MockkBean
    lateinit var messageService: MessageService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun createMessage_regularScenario_successfulResponse() {
        // given
        justRun { messageService.sendMessage(any()) }
        val messageJsonStr = sampleMessage("420", "6")

        // when
        val result = mockMvc.post("/users/420/messages") {
            header("USER_ID_AUTH", "420")
            contentType = MediaType.APPLICATION_JSON
            content = messageJsonStr
        }

        // then
        verify {
            messageService.sendMessage(
                MessageDto(
                    420, 6, "This is a regular sample message",
                    LocalDateTime.parse("2022-04-01T13:30:00").toInstant(ZoneOffset.UTC)
                )
            )
        }
        result.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun createMessage_specialCharactersMessage_successfulResponse() {
        // given
        justRun { messageService.sendMessage(any()) }
        val messageJsonStr = sampleJsonReservedCharactersMessage()

        // when
        val result = mockMvc.post("/users/420/messages") {
            header("USER_ID_AUTH", "420")
            contentType = MediaType.APPLICATION_JSON
            content = messageJsonStr
        }
        // then
        verify {
            messageService.sendMessage(
                MessageDto(
                    420, 6, "\"hello\", {\"another\": \"value\"}",
                    LocalDateTime.parse("2022-04-01T13:30:00").toInstant(ZoneOffset.UTC)
                )
            )
        }
        result.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun createMessage_messageSentToSelf_badRequest() {
        // given
        every { messageService.sendMessage(any()) }.throws(InvalidRecipientException())
        val messageJsonStr = sampleMessage("420", "420")

        // when
        val result = mockMvc.post("/users/420/messages") {
            header("USER_ID_AUTH", "420")
            contentType = MediaType.APPLICATION_JSON
            content = messageJsonStr
        }

        // then
        result.andExpect {
            status { isBadRequest() }
            content {
                json(
                    """
                        {"reason": "Invalid recipient"}
                    """.trimIndent()
                )
            }
        }
    }

    @Test
    fun sentMessages_allMessagesForUser_messagesRetrieved() {
        // given
        every { messageService.retrieveSentMessages(420) }.returns(sentMessageDtos())
        // when
        val result = mockMvc.get("/users/420/sent-messages") {
            header("USER_ID_AUTH", "420")
        }
        // then
        verify { messageService.retrieveSentMessages(420) }
        result.andExpect {
            status { isOk() }
            content { json(sentMessages()) }
        }
    }

    @Test
    fun receivedMessages_allMessagesOfUser_messagesRetrieved() {
        // given
        every { messageService.retrieveReceivedMessages(420, -1) }.returns(receivedMessageDtos())

        // when
        val result = mockMvc.get("/users/420/received-messages") {
            header("USER_ID_AUTH", "420")
        }

        // then
        verify { messageService.retrieveReceivedMessages(420, -1) }
        result.andExpect {
            status { isOk() }
            content { json(receivedMessages()) }
        }
    }

    @Test
    fun receivedMessages_allMessagesFromSpecificUser_messagesRetrieved() {
        // given
        every { messageService.retrieveReceivedMessages(420, 6) }.returns(
            listOf(
                receivedMessageDtos()[0],
                receivedMessageDtos()[3]
            )
        )

        // when
        val result = mockMvc.get("/users/420/received-messages?from=6") {
            header("USER_ID_AUTH", "420")
        }

        // then
        verify { messageService.retrieveReceivedMessages(420, 6) }
        result.andExpect {
            status { isOk() }
            content { json(receivedSpecificMessages()) }
        }
    }

    @Test
    fun receivedMessages_wrongUserIdHeader_messagesRetrieved() {
        mockMvc.get("/users/420/received-messages?from=6") {
            header("USER_ID_AUTH", "421")
        }
            .andExpect {
                status { isForbidden() }
                content { json("{\"reason\": \"Forbidden access to resource\"}") }
            }
    }

    private fun sampleMessage(from: String, to: String) = """
        {
            "from": $from,
            "to": $to,
            "content": "This is a regular sample message",
            "timestamp": "2022-04-01T13:30:00Z"
        }
    """.trimIndent()

    private fun sampleJsonReservedCharactersMessage() = """
        {
            "from": 420,
            "to": 6,
            "content": "\"hello\", {\"another\": \"value\"}",
            "timestamp": "2022-04-01T13:30:00Z"
        }
    """.trimIndent()

    private fun sentMessages() = """
        [
            {
                "from": 420,
                "to": 6,
                "content": "Hello",
                "timestamp": "2022-04-01T13:30:00Z"
            },
            {
                "from": 420,
                "to": 7,
                "content": "it's me",
                "timestamp": "2022-04-01T13:31:00Z"
            },
            {
                "from": 420,
                "to": 8,
                "content": "I was wondering",
                "timestamp": "2022-04-01T13:32:00Z"
            },
            {
                "from": 420,
                "to": 6,
                "content": "if after all these years you'd like to meet",
                "timestamp": "2022-04-01T13:33:00Z"
            }
        ]
    """.trimIndent()

    private fun receivedMessages() = """
        [
            {
                "from": 6,
                "to": 420,
                "content": "you used to get it in your fishnets",
                "timestamp": "2022-04-01T13:30:00Z"
            },
            {
                "from": 7,
                "to": 420,
                "content": "now you only get it in your night dress",
                "timestamp": "2022-04-01T13:31:00Z"
            },
            {
                "from": 8,
                "to": 420,
                "content": "discarded all the naughty nights for niceness",
                "timestamp": "2022-04-01T13:32:00Z"
            },
            {
                "from": 6,
                "to": 420,
                "content": "landed in a very common crisis",
                "timestamp": "2022-04-01T13:33:00Z"
            }
        ]
    """.trimIndent()

    private fun receivedSpecificMessages() = """
        [
            {
                "from": 6,
                "to": 420,
                "content": "you used to get it in your fishnets",
                "timestamp": "2022-04-01T13:30:00Z"
            },
            {
                "from": 6,
                "to": 420,
                "content": "landed in a very common crisis",
                "timestamp": "2022-04-01T13:33:00Z"
            }
        ]
    """.trimIndent()

    private fun sentMessageDtos() = listOf(
        MessageDto(
            420,
            6,
            "Hello",
            LocalDateTime.parse("2022-04-01T13:30:00.000").toInstant(ZoneOffset.UTC)
        ),
        MessageDto(
            420,
            7,
            "it's me",
            LocalDateTime.parse("2022-04-01T13:31:00.000").toInstant(ZoneOffset.UTC)
        ),
        MessageDto(
            420,
            8,
            "I was wondering",
            LocalDateTime.parse("2022-04-01T13:32:00.000").toInstant(ZoneOffset.UTC)
        ),
        MessageDto(
            420,
            6,
            "if after all these years you'd like to meet",
            LocalDateTime.parse("2022-04-01T13:33:00.000").toInstant(ZoneOffset.UTC)
        )
    )

    private fun receivedMessageDtos() = listOf(
        MessageDto(
            6,
            420,
            "you used to get it in your fishnets",
            LocalDateTime.parse("2022-04-01T13:30:00").toInstant(ZoneOffset.UTC)
        ),
        MessageDto(
            7,
            420,
            "now you only get it in your night dress",
            LocalDateTime.parse("2022-04-01T13:31:00").toInstant(ZoneOffset.UTC)
        ),
        MessageDto(
            8,
            420,
            "discarded all the naughty nights for niceness",
            LocalDateTime.parse("2022-04-01T13:32:00").toInstant(ZoneOffset.UTC)
        ),
        MessageDto(
            6,
            420,
            "landed in a very common crisis",
            LocalDateTime.parse("2022-04-01T13:33:00").toInstant(ZoneOffset.UTC)
        )
    )
}
