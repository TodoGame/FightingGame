package com.somegame.security

import com.somegame.user.UserEntity
import security.UserLoginInput

object UserSource {
    val user1 = UserEntity("user1", "pass1", "User1")
    val user2 = UserEntity("user2", "pass2", "User2")
    private val users = listOf<UserEntity>(user1, user2).associateBy { it.username }.toMutableMap()
    fun findUserByUsername(username: String): UserEntity? = users[username]

    fun findUserByCredentials(credentials: UserLoginInput): UserEntity? = findUserByUsername(credentials.username)

    fun registerUser(user: UserEntity) {
        if (users[user.username] == null) {
            users[user.username] = user
        } else {
            throw UserAlreadyExists()
        }
    }

    class UserAlreadyExists : IllegalArgumentException("User already exists")
}
