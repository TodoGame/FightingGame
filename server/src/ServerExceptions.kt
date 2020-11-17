package com.somegame

sealed class ServerException(message: String) : Exception(message)

open class ConflictException(message: String) : ServerException(message)
