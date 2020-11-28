package com.somegame

import com.somegame.responseExceptions.BadRequestException
import io.ktor.features.*
import kotlinx.serialization.SerializationException

suspend fun <R> handleReceiveExceptions(func: suspend () -> R) = try {
    func()
} catch (e: ContentTransformationException) {
    throw BadRequestException("Could not transform request: $e")
} catch (e: SerializationException) {
    throw BadRequestException("Could not deserialize request: $e")
}
