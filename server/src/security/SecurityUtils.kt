package com.somegame.security

import com.somegame.responseExceptions.UnauthorizedException
import com.somegame.user.User
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import org.koin.core.KoinComponent
import org.koin.core.inject

object SecurityUtils : KoinComponent {
    fun ApplicationCall.userPrinciple() = principal<UserPrincipal>() ?: throw UnauthorizedException()

    fun ApplicationCall.user(): User {
        val userService: UserService by inject()
        return userService.findUserByUsername(userPrinciple().username) ?: throw UnauthorizedException()
    }
}
