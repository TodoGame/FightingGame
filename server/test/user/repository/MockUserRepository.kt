package com.somegame.user.repository

import security.UserRegisterInput
import user.Username

class MockUserRepository : UserRepository {

    companion object {
        val user1 = MockUserEntity("user1", "pass1", "User1")
        val user2 = MockUserEntity("user2", "pass2", "User2")
        val fakeUser = MockUserEntity("fakeUser", "password", "Fake User")
    }

    private val users = mutableListOf<UserEntity>(user1, user2)

    override fun findUserByUsername(username: Username) = users.find { it.username == username }

    override fun createUser(input: UserRegisterInput): UserEntity {
        val user = MockUserEntity(input.username, input.password, input.name)
        users.add(user)
        return user
    }

    fun makeNewTestUser(): UserEntity {
        val id = users.size
        return createUser(UserRegisterInput("user$id", "pass$id", "User$id"))
    }

    fun makeNewTestUser(username: Username): UserEntity {
        return createUser(UserRegisterInput(username, "testPassword", username.capitalize()))
    }

    fun clear() {
        users.clear()
        users.add(user1)
        users.add(user2)
    }
}
