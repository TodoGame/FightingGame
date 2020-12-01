package com.somegame.user

import com.somegame.security.SecurityUtils.user
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import user.GET_ME_ENDPOINT

fun Routing.user() {
    authenticate {
        get(GET_ME_ENDPOINT) {
            val user = call.user()
            call.respond(user.publicData())
        }
    }
}
