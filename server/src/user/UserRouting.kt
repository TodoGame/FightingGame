package com.somegame.user

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import user.User

fun Routing.user() {
    authenticate {
        get("/me") {
            val user = call.principal<User>()
            if (user != null) {
                call.respond(user)
            } else {
                call.response.status(HttpStatusCode.Unauthorized)
            }
        }
        post("/changemyname") {
            val user = call.principal<User>()
            val newName = call.receive<String>()
            if (user != null) {
                user.name = newName
                call.respond(user)
            } else {
                call.response.status(HttpStatusCode.Unauthorized)
            }
        }
    }
}