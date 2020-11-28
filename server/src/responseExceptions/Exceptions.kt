package com.somegame.responseExceptions

import io.ktor.http.*

sealed class ExceptionWithResponse(message: String? = null, val statusCode: HttpStatusCode) : Exception(message)

open class UnauthorizedException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.Unauthorized)

class BadRequestException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.BadRequest)

class ConflictException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.Conflict)

class NotFoundException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.NotFound)

class ForbiddenException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.Forbidden)
