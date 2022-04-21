package com.visable.chat.controllers

import com.visable.chat.controllers.exceptions.ForbiddenAccessException
import com.visable.chat.services.exceptions.DuplicateNicknameException
import com.visable.chat.services.exceptions.InvalidRecipientException
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
        ]
    )
    fun systemBadRequestError(e: Exception, request: WebRequest) =
        handleExceptionInternal(e, wrapResponse(e), HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, request)

    @ExceptionHandler(
        value = [
            ForbiddenAccessException::class,
        ]
    )
    fun forbiddenError(e: Exception, request: WebRequest) =
        handleExceptionInternal(e, wrapResponse(e), HttpHeaders.EMPTY, HttpStatus.FORBIDDEN, request)

    private fun wrapResponse(exception: Exception) = ErrorResponse(exception.message!!)
}

// This error wrapper can be extended with additional properties if the custom error requires them
data class ErrorResponse(val reason: String)
