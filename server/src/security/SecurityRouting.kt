package com.somegame.security

import com.fasterxml.jackson.databind.JsonMappingException
import com.somegame.user.User
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Routing.security() {
    post("/login") {
        val credentials = try {
            call.receive<UserLoginCredentials>()
        } catch (e: ContentTransformationException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }
        val user = UserSource.findUserByCredentials(credentials)
        if (user != null && user.password == credentials.password) {
            val token = JwtConfig.makeToken(user)
            call.response.header("Authorization", "Bearer $token")
            call.respond(user)
        } else {
            call.response.status(HttpStatusCode.Unauthorized)
        }
    }

    post("/register") {
        val user = try {
            call.receive<User>()
        } catch (e: io.ktor.features.ContentTransformationException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        } catch (e: JsonMappingException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }

        try {
            UserSource.registerUser(user)
            val token = JwtConfig.makeToken(user)
            call.response.headers.append("Authorization", "Bearer $token")
            call.respond(user)
        } catch (e: UserSource.UserAlreadyExists) {
            call.response.status(HttpStatusCode.BadRequest)
        }
    }

    authenticate {
        post("/changemypassword") {
            val user = call.principal<User>()
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