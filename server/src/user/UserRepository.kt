package com.somegame.user

import com.somegame.user.tables.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.KoinComponent
import security.UserRegisterInput
import user.Username

class UserRepository : KoinComponent {
    fun findUserByUsername(username: Username): User? {
        return transaction {
            val result = User.find { Users.username eq username }
            if (result.count() == 0L) {
                null
            } else {
                result.first()
            }
        }
    }

    fun createUser(input: UserRegisterInput): User {
        return transaction {
            User.new {
                username = input.username
                password = input.password
                name = input.name
            }
        }
    }
}
