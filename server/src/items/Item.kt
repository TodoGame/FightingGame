package com.somegame.items

import com.somegame.user.Users
import item.ItemData
import item.ItemType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Items : IntIdTable() {
    val name = varchar("name", 30)
    val price = integer("price")
    val type = enumerationByName("type", 30, ItemType::class)
    val damage = integer("damage")
}

object UserItems : IntIdTable() {
    val item = reference("item", Items)
    val user = reference("user", Users)

    override val primaryKey = PrimaryKey(item, user)
}

class ItemRepository {
    fun getItemById(id: Int): Item? = transaction {
        Item.findById(id)
    }

    fun getAllItems(): List<Item> = transaction {
        Item.all().toList()
    }
}

fun ItemRepository.getAllPublicItemData(): List<ItemData> = getAllItems().map { it.publicData() }

class Item(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Item>(Items)

    var name by Items.name
    var price by Items.price
    var type by Items.type
    var damage by Items.damage

    fun getId() = id.value
    override fun toString() = "Item(id=$id, name=$name)"
}

fun Item.publicData() = ItemData(getId(), type, name, price, damage)
