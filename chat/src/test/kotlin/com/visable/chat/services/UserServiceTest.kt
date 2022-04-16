package com.visable.chat.services

import com.visable.chat.controllers.dtos.CreateUserDto
import com.visable.chat.controllers.dtos.UserDto
import com.visable.chat.repositories.UserRepository
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UserServiceTest {

    @MockK
    lateinit var userRepository: UserRepository

    lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository)
    }

    @Test
    fun saveUser_simpleUser_successful() {
        // given
        val userDto = CreateUserDto("tyler.durden")
        // when
        val result = userService.saveUser(userDto)
        // then
        Assertions.assertEquals(UserDto(420, "tyler.durden"), result)
    }

    @Test
    fun saveUser_existingNickname_existingNicknameException() {
        // given
        val userDto = CreateUserDto("tyler.durden")
        // when
        Assertions.assertThrows(DuplicateNicknameException::class.java){
            userService.saveUser(userDto)
        }
        // then
    }
}