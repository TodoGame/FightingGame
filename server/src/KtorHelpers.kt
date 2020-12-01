package com.somegame

import com.somegame.responseExceptions.BadRequestException
import com.somegame.security.JwtConfig
import com.somegame.user.User
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.SerializationException

suspend fun <R> handleReceiveExceptions(func: suspend () -> R) = try {
    func()
} catch (e: ContentTransformationException) {
    throw BadRequestException("Could not transform request: $e")
} catch (e: SerializationException) {
    throw BadRequestException("Could not deserialize request: $e")
}

fun ApplicationCall.requiredIdParameter(): Int = request.queryParameters["id"]?.toIntOrNull()
    ?: throw BadRequestException("Request must contain an `id` query parameter with Int value")

fun PipelineContext<Unit, ApplicationCall>.addJwtToken(user: User) {
    val token = JwtConfig.makeLoginToken(user)
    call.response.headers.append("Authorization", "Bearer $token")
}
