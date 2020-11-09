package com.somegame.security

import com.fasterxml.jackson.databind.JsonMappingException
import com.somegame.user.UserEntity
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import security.*

fun Routing.security() {
    post(LOGIN_ENDPOINT) {
        val credentials = try {
            call.receive<UserLoginInput>()
        } catch (e: ContentTransformationException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }
        val user = UserSource.findUserByCredentials(credentials)
        if (user != null && user.password == credentials.password) {
            val token = JwtConfig.makeLoginToken(user)
            call.response.header("Authorization", "Bearer $token")
            call.respond(user)
        } else {
            call.response.status(HttpStatusCode.Unauthorized)
        }
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
        val user = UserEntity(input.username, input.password, input.name)
        try {
            UserSource.registerUser(user)
            val token = JwtConfig.makeLoginToken(user)
            call.response.headers.append("Authorization", "Bearer $token")
            call.respond(user)
        } catch (e: UserSource.UserAlreadyExists) {
            call.response.status(HttpStatusCode.BadRequest)
        }
    }

    authenticate {
        post(CHANGE_MY_PASSWORD_ENDPOINT) {
            val user = call.principal<UserEntity>()
            val input = call.receive<ChangePasswordInput>()
            if (user != null && input.oldPassword == user.password) {
                user.password = input.newPassword
                call.response.status(HttpStatusCode.Accepted)
            } else {
                call.response.status(HttpStatusCode.Unauthorized)
            }
        }
    }
}
