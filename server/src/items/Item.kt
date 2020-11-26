package com.somegame.items

import item.ItemData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Item(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Item>(Items)

    var name by Items.name
    var price by Items.price

    fun getPublicData() = ItemData(id.value, name, price)

    override fun toString() = "Item(id=$id, name=$name)"
}
