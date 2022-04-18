package com.visable.chat.controllers

import com.visable.chat.services.DuplicateNicknameException
import com.visable.chat.services.InvalidRecipientException
import com.visable.chat.services.UserNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(
        value = [
            DuplicateNicknameException::class,
            InvalidRecipientException::class,
            UserNotFoundException::class
        ]
    )
    fun duplicateNickname(e: DuplicateNicknameException, request: WebRequest) =
        handleExceptionInternal(e, e.message, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, request)
}