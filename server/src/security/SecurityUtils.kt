package com.somegame.security

import com.somegame.responseExceptions.UnauthorizedException
import com.somegame.user.User
import com.somegame.user.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import org.koin.core.KoinComponent
import org.koin.core.inject

object SecurityUtils : KoinComponent {
    fun ApplicationCall.userPrincipal() = principal<UserPrincipal>() ?: throw UnauthorizedException()

    fun ApplicationCall.user(): User {
        val userRepository: UserRepository by inject()
        return userRepository.findUserByUsername(userPrincipal().username) ?: throw UnauthorizedException()
    }
}
