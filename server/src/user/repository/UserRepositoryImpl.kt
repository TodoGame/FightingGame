package com.somegame.user.repository

import com.somegame.user.tables.UsersTable
import security.UserRegisterInput
import user.Username

class UserRepositoryImpl : UserRepository {
    override fun findUserByUsername(username: Username): UserEntity? {
        val result = UserEntityImpl.find { UsersTable.username eq username }
        return if (result.count() == 0L) {
            null
        } else {
            result.first()
        }
    }

    override fun createUser(input: UserRegisterInput): UserEntity {
        return UserEntityImpl.new {
            username = input.username
            password = input.password
            name = input.name
        }
    }
}
