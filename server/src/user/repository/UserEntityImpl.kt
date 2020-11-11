package com.somegame.user.repository

import com.somegame.security.UserPrincipal
import com.somegame.user.tables.UsersTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import user.User
import user.UserData
import user.Username

class UserEntityImpl(id: EntityID<Int>) : IntEntity(id), User, UserEntity {
    companion object : IntEntityClass<UserEntityImpl>(UsersTable)

    override var username: Username by UsersTable.username
    override var password by UsersTable.password
    override var name by UsersTable.name

    override fun getPrincipal() = UserPrincipal(username)
    override fun getPublicData() = UserData(username, name)

    override fun toString() = "User(username=$username)"
}
