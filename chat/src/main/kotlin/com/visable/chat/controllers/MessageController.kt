package com.visable.chat.controllers

import com.visable.chat.controllers.dtos.MessageDto
import com.visable.chat.controllers.exceptions.ForbiddenAccessException
import com.visable.chat.services.MessageService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class MessageController(val messageService: MessageService) {

    @PostMapping("/users/{userId}/messages")
    @ResponseStatus(code = HttpStatus.CREATED)
    fun createMessage(
        @RequestHeader("USER_ID_AUTH") principalId: Long,
        @PathVariable userId: Long,
        @RequestBody messageDto: MessageDto
    ) {
        validatePrincipal(principalId, userId)
        messageDto.from = userId
        messageService.sendMessage(messageDto)
    }

    @GetMapping("/users/{userId}/sent-messages")
    fun sentMessages(@RequestHeader("USER_ID_AUTH") principalId: Long, @PathVariable userId: Long): List<MessageDto> {
        validatePrincipal(principalId, userId)
        return messageService.retrieveSentMessages(userId)
    }

    @GetMapping("/users/{userId}/received-messages")
    fun receivedMessages(
        @RequestHeader("USER_ID_AUTH") principalId: Long,
        @PathVariable userId: Long,
        @RequestParam("from") from: Long?
    ): List<MessageDto> {
        validatePrincipal(principalId, userId)
        return messageService.retrieveReceivedMessages(userId, from ?: -1)
    }

    private fun validatePrincipal(principalId: Long, userId: Long) {
        if (principalId != userId)
            throw ForbiddenAccessException()
    }

}
