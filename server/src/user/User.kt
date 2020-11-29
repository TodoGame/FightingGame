package com.somegame.user

import com.somegame.faculty.Faculty
import com.somegame.items.Item
import com.somegame.items.ItemExtensions.publicData
import com.somegame.user.tables.UserItems
import com.somegame.user.tables.Users
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import user.Username

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var username: Username by Users.username
    var password by Users.password
    var name by Users.name

    var faculty by Faculty referencedOn Users.faculty

    var inventory by Item via UserItems

    var money by Users.money

    fun hasItem(item: Item) = transaction {
        inventory.find { it.id == item.id } != null
    }

    fun addToInventory(item: Item) = transaction {
        inventory = SizedCollection(inventory + listOf(item))
    }

    fun acceptMoney(amount: Int) = transaction {
        money += amount
    }

    fun spendMoney(amount: Int) = transaction {
        money -= amount
    }

    fun publicInventory() = transaction { inventory.map { it.publicData() } }

    override fun toString() = "User(id=$id, username=$username)"
}
