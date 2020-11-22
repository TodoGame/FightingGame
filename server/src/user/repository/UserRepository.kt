package com.somegame.user.repository

import security.UserRegisterInput
import user.Username
import java.lang.IllegalArgumentException
import kotlin.reflect.full.IllegalPropertyDelegateAccessException

interface UserRepository {
    fun findUserByUsername(username: Username): UserEntity?

    fun createUser(input: UserRegisterInput): UserEntity

    fun addItemToInventory(username: Username, itemId: Int): UserEntity

    class UserNotFoundException(username: Username) : IllegalArgumentException("User with username=$username not found")

    class ItemNotFoundException(itemId: Int) : IllegalArgumentException("Item with id=$itemId not found")
}
