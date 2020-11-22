package com.somegame.items.repository

import org.jetbrains.exposed.sql.transactions.transaction

class ItemRepositoryImpl : ItemRepository {
    override fun getItemById(id: Int): ItemEntity? = transaction {
        ItemEntityImpl.findById(id)
    }
}
