package com.somegame.security

import com.somegame.user.User
import io.ktor.auth.*

class UserSource {
    private val users = listOf(User("username", "password")).associateBy { it.username }.toMutableMap()

    fun findUserByUsername(username: String): User? = users[username]

    fun findUserByCredentials(credentials: UserPasswordCredential): User? = findUserByUsername(credentials.name)

    fun registerUser(user: User) {
        if (users[user.username] == null) {
            users[user.username] = user
        } else {
            throw UserAlreadyExists()
        }
    }

    class UserAlreadyExists : IllegalArgumentException("User already exists")
}
