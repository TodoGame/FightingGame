package com.somegame.user.repository

import security.UserRegisterInput
import user.Username

interface UserRepository {
    fun findUserByUsername(username: Username): UserEntity?

    fun createUser(input: UserRegisterInput): UserEntity
}
