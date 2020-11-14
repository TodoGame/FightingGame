package com.somegame.user.repository

import com.somegame.user.tables.Users
import org.jetbrains.exposed.sql.transactions.transaction
import security.UserRegisterInput
import user.Username

class UserRepositoryImpl : UserRepository {
    override fun findUserByUsername(username: Username): UserEntity? {
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
}
