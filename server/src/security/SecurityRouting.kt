package com.somegame.security

import com.fasterxml.jackson.databind.JsonMappingException
import com.somegame.user.repository.UserEntity
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.koin.ktor.ext.inject
import security.*

fun Routing.security() {
    val userService: UserService by inject()

    post(LOGIN_ENDPOINT) {
        val input = try {
            call.receive<UserLoginInput>()
        } catch (e: ContentTransformationException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }
        val user = try {
            userService.loginUser(input)
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            return@post
        }

        addJwtToken(user)
        call.respond(user.getPublicData())
    }

    post(REGISTER_ENDPOINT) {
        val input = try {
            call.receive<UserRegisterInput>()
        } catch (e: io.ktor.features.ContentTransformationException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        } catch (e: JsonMappingException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }

        val newUser = try {
            userService.registerUser(input)
        } catch (e: UserService.UserAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict, "User with this username already exists")
            return@post
        }
        addJwtToken(newUser)
        call.respond(newUser.getPublicData())
    }
}

fun PipelineContext<Unit, ApplicationCall>.addJwtToken(user: UserEntity) {
    val token = JwtConfig.makeLoginToken(user)
    call.response.headers.append("Authorization", "Bearer $token")
}
