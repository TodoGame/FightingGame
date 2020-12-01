package com.somegame.user

import com.somegame.responseExceptions.BadRequestException
import com.somegame.responseExceptions.NotFoundException
import com.somegame.security.SecurityUtils.user
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import user.GET_ME_ENDPOINT
import user.GET_USER

const val USERNAME_PARAMETER = "username"

fun Routing.user() {

    val userService: UserService by inject()

    authenticate {
        get(GET_ME_ENDPOINT) {
            val user = call.user()
            call.respond(user.publicData())
        }

        get(GET_USER) {
            val username = call.request.queryParameters[USERNAME_PARAMETER]
                ?: throw BadRequestException("Request must have a query parameter `username`")
            val user = userService.findUserByUsername(username)
                ?: throw NotFoundException("User with username=$username not found")
            call.respond(user.publicData())
        }
    }
}
