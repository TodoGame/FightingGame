package com.somegame.security

import com.somegame.handleReceiveExceptions
import com.somegame.responseExceptions.ConflictException
import com.somegame.responseExceptions.UnauthorizedException
import com.somegame.user.User
import com.somegame.user.UserExtensions.publicData
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.ktor.ext.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import security.*

object SecurityRouting : KoinComponent {
    private val loginLogger: Logger = LoggerFactory.getLogger(LOGIN_ENDPOINT)
    private val registerLogger: Logger = LoggerFactory.getLogger(REGISTER_ENDPOINT)

    fun Routing.security() {
        post(LOGIN_ENDPOINT) {
            val input = handleReceiveExceptions { call.receive<UserLoginInput>() }
            val user = loginUser(input)

            addJwtToken(user)
            call.respond(user.publicData())
        }

        post(REGISTER_ENDPOINT) {
            val input = handleReceiveExceptions { call.receive<UserRegisterInput>() }

            val newUser = registerUser(input)

            addJwtToken(newUser)
            call.response.status(HttpStatusCode.Created)
            call.respond(newUser.publicData())
        }
    }

    private fun loginUser(input: UserLoginInput): User {
        val userService: UserService by inject()

        return try {
            userService.loginUser(input)
        } catch (e: UserService.InvalidLoginInputException) {
            throw UnauthorizedException("Invalid credentials")
        }
    }

    private fun registerUser(input: UserRegisterInput) = try {
        val userService: UserService by inject()

        userService.registerUser(input)
    } catch (e: UserService.UserAlreadyExistsException) {
        throw ConflictException("User with this username already exists")
    }
}

fun PipelineContext<Unit, ApplicationCall>.addJwtToken(user: User) {
    val token = JwtConfig.makeLoginToken(user)
    call.response.headers.append("Authorization", "Bearer $token")
}
