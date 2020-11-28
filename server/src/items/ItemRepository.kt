package com.somegame.items

import org.jetbrains.exposed.sql.transactions.transaction

class ItemRepository {
    fun getItemById(id: Int): Item? = transaction {
        Item.findById(id)
    }
}
