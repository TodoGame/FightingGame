package com.somegame

import com.somegame.security.JwtConfig
import com.somegame.user.service.UserService
import io.ktor.server.testing.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import user.Username

object TestUtils : KoinComponent {

    private val userService: UserService by inject()

    fun TestApplicationRequest.addJwtHeader(username: Username) {
        val userEntity = userService.findUserByUsername(username) ?: throw IllegalArgumentException()
        val token = JwtConfig.makeLoginToken(userEntity)
        addHeader("Authorization", "Bearer $token")
    }
}
