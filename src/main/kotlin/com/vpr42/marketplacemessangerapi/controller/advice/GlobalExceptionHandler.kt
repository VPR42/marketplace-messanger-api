package com.vpr42.marketplacemessangerapi.controller.advice

import com.vpr42.marketplacemessangerapi.dto.response.ErrorResponse
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Hidden
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MissingRequestCookieException::class)
    fun handleMissingCookie(
        request: HttpServletRequest,
        ex: MissingRequestCookieException,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("MissingRequestCookieException: ${ex.message}")
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value().toString(),
            message = ex.message,
            path = request.requestURI
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        request: HttpServletRequest,
        ex: IllegalArgumentException,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("IllegalArgumentException: ${ex.message}")
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value().toString(),
            message = ex.message ?: "Some argument is illegal",
            path = request.requestURI
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        request: HttpServletRequest,
        ex: Exception,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception: ${ex.message}", ex)
        logger.debug(ex.stackTraceToString())
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value().toString(),
            message = ex.message ?: "Unexpected exception",
            path = request.requestURI
        )
        return ResponseEntity.badRequest().body(response)
    }
}
