package com.somegame.user.repository

import com.somegame.items.repository.ItemRepository
import items.repository.MockItemEntity
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.java.KoinJavaComponent.inject
import security.UserRegisterInput
import user.Username

class MockUserRepository : UserRepository, KoinComponent {

    private val itemRepository: ItemRepository by inject()

    companion object {
        val user1 = MockUserEntity("user1", "pass1", "User1")
        val user2 = MockUserEntity("user2", "pass2", "User2")
        val fakeUser = MockUserEntity("fakeUser", "password", "Fake User")
    }

    private val users = mutableListOf<MockUserEntity>(user1, user2)

    override fun findUserByUsername(username: Username) = users.find { it.username == username }

    override fun createUser(input: UserRegisterInput): UserEntity {
        val user = MockUserEntity(input.username, input.password, input.name)
        users.add(user)
        return user
    }

    override fun addItemToInventory(username: Username, itemId: Int): UserEntity {
        val user = findUserByUsername(username) ?: throw UserRepository.UserNotFoundException(username)
        val item = itemRepository.getItemById(itemId) ?: throw UserRepository.ItemNotFoundException(itemId)
        user.addToInventory(item as MockItemEntity)
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
