package com.visable.chat.controllers

import com.visable.chat.controllers.dtos.CreateUserDto
import com.visable.chat.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(val userService: UserService) {

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    fun createUser(@RequestBody userDto: CreateUserDto) = userService.saveUser(userDto)
}