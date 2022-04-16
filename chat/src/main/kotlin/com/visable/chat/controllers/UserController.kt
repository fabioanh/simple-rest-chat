package com.visable.chat.controllers

import com.visable.chat.services.UserService
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(val userService: UserService) {

}