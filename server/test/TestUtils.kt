package com.somegame

import com.somegame.security.JwtConfig
import com.somegame.user.UserRepository
import io.ktor.server.testing.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import user.Username

object TestUtils : KoinComponent {
    fun TestApplicationRequest.addJwtHeader(username: Username) {
        val userRepository: UserRepository by inject()

        val userEntity = userRepository.findUserByUsername(username) ?: throw IllegalArgumentException()
        val token = JwtConfig.makeLoginToken(userEntity)
        addHeader("Authorization", "Bearer $token")
    }

    fun TestApplicationRequest.addJsonContentHeader() {
        addHeader("Content-Type", "application/json")
    }
}
