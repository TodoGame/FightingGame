package com.somegame.user.repository

import com.somegame.security.UserPrincipal
import user.User
import user.UserData
import user.Username

interface UserEntity : User {
    override val username: Username
    val password: String
    override val name: String

    fun getPrincipal(): UserPrincipal

    fun getPublicData(): UserData
}
