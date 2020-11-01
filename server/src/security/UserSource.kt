package com.somegame.security

import user.User

object UserSource {
    val user1 = User("user1", "pass1", "User1")
    val user2 = User("user2", "pass2", "User2")
    private val users = listOf<User>(user1, user2).associateBy { it.username }.toMutableMap()
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
