package com.visable.chat.services

import com.visable.chat.controllers.dtos.CreateUserDto
import com.visable.chat.controllers.dtos.UserDto
import com.visable.chat.data.User
import com.visable.chat.repositories.UserRepository
import org.hibernate.exception.ConstraintViolationException
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {
    fun saveUser(userDto: CreateUserDto): UserDto {
        try {
            return userRepository.save(User(0, userDto.nickname)).toDto()
        } catch (e: ConstraintViolationException) {
            throw DuplicateNicknameException()
        }
    }

}
