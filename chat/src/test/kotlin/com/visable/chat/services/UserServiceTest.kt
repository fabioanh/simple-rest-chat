package com.visable.chat.services

import com.visable.chat.controllers.dtos.CreateUserDto
import com.visable.chat.controllers.dtos.UserDto
import com.visable.chat.data.User
import com.visable.chat.repositories.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.sql.SQLException

internal class UserServiceTest {

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var userService: UserService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun saveUser_simpleUser_successful() {
        // given
        every { userRepository.save(any()) }.returns(User(420, "tyler.durden"))
        val userDto = CreateUserDto("tyler.durden")
        // when
        val result = userService.saveUser(userDto)
        // then
        Assertions.assertEquals(UserDto(420, "tyler.durden"), result)
        verify { userRepository.save(User(0, "tyler.durden")) }
    }

    @Test
    fun saveUser_existingNickname_existingNicknameException() {
        // given
        every { userRepository.save(any()) }.throws(DataIntegrityViolationException(""))
        val userDto = CreateUserDto("tyler.durden")
        // when
        Assertions.assertThrows(DuplicateNicknameException::class.java) {
            userService.saveUser(userDto)
        }
        // then
    }
}