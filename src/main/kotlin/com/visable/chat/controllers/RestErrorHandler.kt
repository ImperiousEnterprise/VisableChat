package com.visable.chat.controllers

import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.validation.BindingResultUtils.getBindingResult
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import kotlin.collections.ArrayList
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.validation.ValidationUtils
import org.springframework.web.HttpRequestMethodNotSupportedException
import java.lang.Exception
import java.net.ConnectException
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

class NotExistException(message: String):Exception(message)
class SelfMessageException(message: String):Exception(message)

@ControllerAdvice
class RestErrorHandler{

    class ValidationError(val error: String)


    @Bean(name = arrayOf("messageSource"))
    fun messageSource(): ReloadableResourceBundleMessageSource {
        val messageBundle = ReloadableResourceBundleMessageSource()
        messageBundle.setBasename("classpath:messages/messages")
        messageBundle.setDefaultEncoding("UTF-8")
        return messageBundle
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processValidationError(ex: MethodArgumentNotValidException): ValidationError {
        val result : BindingResult = ex.bindingResult
        val fieldErrors: List<FieldError> = result.fieldErrors
        return generateValidationError(fieldErrors[0].defaultMessage.toString())
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processValidationError(e: ConstraintViolationException): ValidationError {
        val violations = e.constraintViolations
        val violation = violations.iterator().next()
        return generateValidationError(violation.message)
    }

    @ExceptionHandler(ConnectException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun processValidationError(ex: ConnectException): ValidationError {
        return generateValidationError(ex.localizedMessage)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    fun processValidationError(ex: HttpRequestMethodNotSupportedException): ValidationError {
        return generateValidationError("error.http.notallowed")
    }

    @ExceptionHandler(Exception::class,NotExistException::class, SelfMessageException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processValidationError(e: Exception): ValidationError {
        return generateValidationError(e.localizedMessage)
    }

    fun generateValidationError(message: String):ValidationError{
        val currentLocale = LocaleContextHolder.getLocale()
        var localizedErrorMessage = messageSource().getMessage(message, null, currentLocale)
        return ValidationError(localizedErrorMessage)
    }
}