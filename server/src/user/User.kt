package com.somegame.user

import com.somegame.items.Item
import com.somegame.security.UserPrincipal
import com.somegame.user.tables.UserItems
import com.somegame.user.tables.Users
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import user.UserData
import user.Username

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var username: Username by Users.username
    var password by Users.password
    var name by Users.name

    var inventory by Item via UserItems

    fun addToInventory(item: Item) = transaction {
        if (item in inventory) {
            throw ItemAlreadyInInventoryException(item)
        }
        inventory = SizedCollection(inventory + listOf(item))
    }

    fun getPrincipal() = UserPrincipal(username)
    fun getPublicData() = transaction { UserData(username, name, inventory.map { it.getPublicData() }) }

    override fun toString() = "User(id=$id, username=$username)"
    
    class ItemAlreadyInInventoryException(item: Item) : IllegalArgumentException("Item $item already in inventory")
}
