package com.somegame.user.repository

import com.somegame.items.repository.ItemEntityImpl
import com.somegame.items.repository.ItemRepository
import com.somegame.user.tables.Users
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.KoinComponent
import org.koin.core.inject
import security.UserRegisterInput
import user.Username

class UserRepositoryImpl : UserRepository, KoinComponent {
    private val itemRepository: ItemRepository by inject()

    override fun findUserByUsername(username: Username): UserEntityImpl? {
        return transaction {
            val result = UserEntityImpl.find { Users.username eq username }
            if (result.count() == 0L) {
                null
            } else {
                result.first()
            }
        }
    }

    override fun createUser(input: UserRegisterInput): UserEntity {
        return transaction {
            UserEntityImpl.new {
                username = input.username
                password = input.password
                name = input.name
            }
        }
    }

    override fun addItemToInventory(username: Username, itemId: Int): UserEntity {
        val user = findUserByUsername(username) ?: throw UserRepository.UserNotFoundException(username)
        val item =
            itemRepository.getItemById(itemId) as ItemEntityImpl? ?: throw UserRepository.ItemNotFoundException(itemId)
        return transaction {
            user.inventory = SizedCollection(user.inventory + listOf(item))
            user
        }
    }
}
