package com.somegame.user.repository

import com.somegame.items.ItemRepository
import com.somegame.user.User
import com.somegame.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.koin.core.KoinComponent
import org.koin.core.inject
import security.UserRegisterInput
import user.Username

object MockUserRepositoryFactory : KoinComponent {
    val user1 = MockUserFactory.create("user1", "pass1", "User1")
    val user2 = MockUserFactory.create("user2", "pass2", "User2")
    val fakeUser = MockUserFactory.create("fakeUser", "password", "Fake User")

    private val users = mutableListOf<User>(user1, user2)

    fun create(): UserRepository {
        val userRepository = mockk<UserRepository>()

        val users = mutableListOf(user1, user2)

        every { userRepository.findUserByUsername(any()) } answers { users.find { it.username == firstArg<Username>() } }
        every { userRepository.createUser(any()) } answers {
            val registerInput = firstArg<UserRegisterInput>()
            val user = MockUserFactory.create(registerInput.username, registerInput.password, registerInput.name)
            users.add(user)
            user
        }
        return userRepository
    }

    fun UserRepository.makeNewTestUser(username: Username): User {
        return createUser(UserRegisterInput(username, "testPassword", username.capitalize()))
    }
}
