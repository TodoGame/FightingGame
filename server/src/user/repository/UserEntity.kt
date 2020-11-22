package com.somegame.user.repository

import com.somegame.items.repository.ItemEntity
import com.somegame.security.UserPrincipal
import org.jetbrains.exposed.sql.SizedIterable
import user.User
import user.UserData
import user.Username

interface UserEntity : User {
    override val username: Username
    val password: String
    override val name: String

    val inventory: SizedIterable<ItemEntity>

    fun getPrincipal(): UserPrincipal

    fun getPublicData(): UserData
}
