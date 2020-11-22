package com.somegame.items.repository

import com.somegame.items.tables.Items
import item.ItemData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ItemEntityImpl(id: EntityID<Int>): IntEntity(id), ItemEntity {
    companion object : IntEntityClass<ItemEntityImpl>(Items)

    override fun getId() = id.value

    override var name by Items.name
    override var price by Items.price

    fun getPublicData() = ItemData(id.value, name, price)

    override fun toString() = "Item(id=$id, name=$name)"
}
