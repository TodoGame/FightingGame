package com.somegame.user

import com.somegame.security.UserPrincipal
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import user.GET_ME_ENDPOINT

fun Routing.user() {
    val userService: UserService by inject()

    authenticate {
        get(GET_ME_ENDPOINT) {
            val userPrincipal = call.principal<UserPrincipal>()
            val user = userPrincipal?.let { userService.findUserByUsername(it?.username) }
            if (user != null) {
                call.respond(user)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }
        }
    }
}
