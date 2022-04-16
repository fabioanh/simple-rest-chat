package com.visable.chat.controllers

import com.visable.chat.services.MessageService
import org.springframework.web.bind.annotation.RestController

@RestController
class MessageController(val messageService: MessageService) {

}