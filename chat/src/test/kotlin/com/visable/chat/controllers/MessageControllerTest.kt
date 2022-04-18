package com.visable.chat.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(UserController::class)
class MessageControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun createMessage_regularScenario_successfulResponse() {
        val messageJsonStr = sampleMessage("420", "6")
        mockMvc.post("/users/420/messages") {
            header("USER_ID_AUTH", "420")
            contentType = MediaType.APPLICATION_JSON
            content = messageJsonStr
        }
            .andExpect {
                status { isCreated() }
            }
    }

    @Test
    fun createMessage_specialCharactersMessage_successfulResponse() {
        val messageJsonStr = sampleJsonReservedCharactersMessage()
        mockMvc.post("/users/420/messages") {
            header("USER_ID_AUTH", "420")
            contentType = MediaType.APPLICATION_JSON
            content = messageJsonStr
        }
            .andExpect {
                status { isCreated() }
            }
    }

    @Test
    fun createMessage_messageSentToSelf_badRequest() {
        val messageJsonStr = sampleMessage("420", "420")
        mockMvc.post("/users/420/messages") {
            header("USER_ID_AUTH", "420")
            contentType = MediaType.APPLICATION_JSON
            content = messageJsonStr
        }
            .andExpect {
                status { isCreated() }
            }
    }

    @Test
    fun sentMessages_allMessagesForUser_messagesRetrieved() {
        mockMvc.get("/users/420/sent-messages"){
            header("USER_ID_AUTH", "420")
        }
            .andExpect {
                status { isOk() }
                content { json(sentMessages()) }
            }
    }

    @Test
    fun receivedMessages_allMessagesOfUser_messagesRetrieved() {
        mockMvc.get("/users/420/received-messages"){
            header("USER_ID_AUTH", "420")
        }
            .andExpect {
                status { isOk() }
                content { json(receivedMessages()) }
            }
    }

    @Test
    fun receivedMessages_allMessagesFromSpecificUser_messagesRetrieved() {
        mockMvc.get("/users/420/received-messages?from=6"){
            header("USER_ID_AUTH", "420")
        }
            .andExpect {
                status { isOk() }
                content { json(receivedSpecificMessages()) }
            }
    }

    @Test
    fun receivedMessages_wrongUserIdHeader_messagesRetrieved() {
        mockMvc.get("/users/420/received-messages?from=6"){
            header("USER_ID_AUTH", "421")
        }
            .andExpect {
                status { isForbidden() }
                content { json("{\"reason\": \"Unauthorised access to resource\"}") }
            }
    }

    private fun sampleMessage(from: String, to: String) = """
        {
            "from": $from,
            "to": $to,
            "content": "This is a regular sample message",
            "timestamp": "2022-04-01T13:30:00.000Z"
        }
    """.trimIndent()

    private fun sampleJsonReservedCharactersMessage() = """
        {
            "from": 420,
            "to": 6,
            "content": "\"hello\", \{\"another\": \"value\"\}",
            "timestamp": "2022-04-01T13:30:00.000Z"
        }
    """.trimIndent()

    private fun sentMessages() = """
        [
            {
                "from": 420,
                "to": 6,
                "content": "Hello",
                "timestamp": "2022-04-01T13:30:00.000Z"
            },
            {
                "from": 420,
                "to": 7,
                "content": "it's me",
                "timestamp": "2022-04-01T13:31:00.000Z"
            },
            {
                "from": 420,
                "to": 8,
                "content": "I was wondering",
                "timestamp": "2022-04-01T13:32:00.000Z"
            },
            {
                "from": 420,
                "to": 6,
                "content": "if after all these years you'd like to meet",
                "timestamp": "2022-04-01T13:33:00.000Z"
            }
        ]
    """.trimIndent()

    private fun receivedMessages() = """
        [
            {
                "from": 6,
                "to": 420,
                "content": "you used to get it in your fishnets",
                "timestamp": "2022-04-01T13:30:00.000Z"
            },
            {
                "from": 7,
                "to": 420,
                "content": "now you only get it in your night dress",
                "timestamp": "2022-04-01T13:31:00.000Z"
            },
            {
                "from": 8,
                "to": 420,
                "content": "discarded all the naughty nights for niceness",
                "timestamp": "2022-04-01T13:32:00.000Z"
            },
            {
                "from": 6,
                "to": 420,
                "content": "landed in a very common crisis",
                "timestamp": "2022-04-01T13:33:00.000Z"
            }
        ]
    """.trimIndent()

    private fun receivedSpecificMessages() = """
        [
            {
                "from": 6,
                "to": 420,
                "content": "you used to get it in your fishnets",
                "timestamp": "2022-04-01T13:30:00.000Z"
            },
            {
                "from": 6,
                "to": 420,
                "content": "landed in a very common crisis",
                "timestamp": "2022-04-01T13:33:00.000Z"
            }
        ]
    """.trimIndent()
}
