package com.visable.chat.services

import com.visable.chat.controllers.dtos.CreateUserDto
import com.visable.chat.controllers.dtos.UserDto
import com.visable.chat.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {
    fun saveUser(userDto: CreateUserDto): UserDto {
        TODO("implement")
    }

}
