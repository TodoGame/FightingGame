package com.somegame

import com.somegame.security.JwtConfig
import com.somegame.security.UserPrincipal
import com.somegame.security.UserSource
import com.somegame.user.UserEntity
import io.ktor.server.testing.*
import user.Username
import kotlin.IllegalArgumentException

object TestUsers {
    val user1 = UserSource.user1
    val user2 = UserSource.user2

    val fakeUser = UserEntity("fakeUser", "password", "Fake User")

    val userPrincipal1 = UserPrincipal(user1.username)
    val userPrincipal2 = UserPrincipal(user2.username)
    val fakeUserPrincipal = UserPrincipal(fakeUser.username)

    fun makeUser(id: Int) = UserEntity("user$id", "pass$id", "User$id")

    fun TestApplicationRequest.addJwtHeader(username: Username) {
        val userEntity = UserSource.findUserByUsername(username) ?: throw IllegalArgumentException()
        val token = JwtConfig.makeLoginToken(userEntity)
        addHeader("Authorization", "Bearer $token")
    }
}
