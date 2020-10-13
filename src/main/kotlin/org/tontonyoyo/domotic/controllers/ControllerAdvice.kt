package org.tontonyoyo.domotic.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.tontonyoyo.domotic.exceptions.FunctionalException
import java.util.*


@ControllerAdvice
class ControllerAdvice {

    @ExceptionHandler(FunctionalException::class)
    fun handleFunctionalException(ex: FunctionalException, request: WebRequest): ResponseEntity<Any> {
        val body: MutableMap<String, Any> = LinkedHashMap()
        body["error"] = ex.msg
        return ResponseEntity(body, HttpStatus.OK)
    }
}