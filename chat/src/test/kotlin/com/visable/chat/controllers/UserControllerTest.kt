package com.visable.chat.controllers

import com.ninjasquad.springmockk.MockkBean
import com.visable.chat.controllers.dtos.CreateUserDto
import com.visable.chat.controllers.dtos.UserDto
import com.visable.chat.services.DuplicateNicknameException
import com.visable.chat.services.UserService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(UserController::class)
class UserControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var userService: UserService

    @Test
    fun createUser_regularOperation_created() {

        every { userService.saveUser(CreateUserDto("tyler.durden")) }
            .returns(UserDto(420, "tyler.durden"))

        val userJsonStr = sampleUser()

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = userJsonStr
        }
            .andExpect {
                status { isCreated() }
                content {
                    json(
                        """
                            {"id":420,"nickname":"tyler.durden"}
                        """.trimIndent()
                    )
                }
            }
    }

    @Test
    fun createUser_duplicateNickname_badRequest() {

        every { userService.saveUser(CreateUserDto("tyler.durden")) }
            .throws(DuplicateNicknameException())

        val userJsonStr = sampleUser()

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = userJsonStr
        }
            .andExpect {
                status { isBadRequest() }
                content {
                    json(
                        """
                            {"reason": "Nickname already present in the system"}
                        """.trimIndent()
                    )
                }
            }
    }

    private fun sampleUser() = "{\"nickname\": \"tyler.durden\" }"

}
