package com.somegame.security

import com.somegame.user.User

object UserSource {
    private val users = listOf<User>().associateBy { it.username }.toMutableMap()
    fun findUserByUsername(username: String): User? = users[username]

    fun findUserByCredentials(credentials: UserLoginCredentials): User? = findUserByUsername(credentials.username)

    fun registerUser(user: User) {
        if (users[user.username] == null) {
            users[user.username] = user
        } else {
            throw UserAlreadyExists()
        }
    }

    class UserAlreadyExists : IllegalArgumentException("User already exists")
}
