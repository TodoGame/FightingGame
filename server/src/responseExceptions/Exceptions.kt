package com.somegame.responseExceptions

import io.ktor.http.*

open class ExceptionWithResponse(message: String? = null, val statusCode: HttpStatusCode) : Exception(message)

open class UnauthorizedException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.Unauthorized)

open class BadRequestException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.BadRequest)

open class ConflictException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.Conflict)

open class NotFoundException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.NotFound)

class ForbiddenException(message: String? = null) : ExceptionWithResponse(message, HttpStatusCode.Forbidden)
