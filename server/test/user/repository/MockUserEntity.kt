package com.somegame.user.repository

import com.somegame.security.UserPrincipal
import items.repository.MockItemEntity
import org.jetbrains.exposed.sql.SizedCollection
import user.UserData
import user.Username

class MockUserEntity(override val username: Username, override val password: String, override val name: String) :
    UserEntity {

    override var inventory = SizedCollection<MockItemEntity>()

    fun addToInventory(item: MockItemEntity) {
        inventory = SizedCollection(inventory + listOf(item))
    }

    override fun getPrincipal() = UserPrincipal(username)

    override fun getPublicData() = UserData(username, name, inventory.map { it.getPublicData() })
}
