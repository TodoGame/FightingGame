package com.somegame.user.repository

import com.somegame.security.UserPrincipal
import com.somegame.user.tables.Users
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import user.User
import user.UserData
import user.Username

class UserEntityImpl(id: EntityID<Int>) : IntEntity(id), User, UserEntity {
    companion object : IntEntityClass<UserEntityImpl>(Users)

    override var username: Username by Users.username
    override var password by Users.password
    override var name by Users.name

    override fun getPrincipal() = UserPrincipal(username)
    override fun getPublicData() = UserData(username, name)

    override fun toString() = "User(username=$username)"
}
