package com.somegame.items.repository

import items.repository.MockItemEntity

class MockItemRepository : ItemRepository {
    val items = mutableListOf<MockItemEntity>()

    override fun getItemById(id: Int): ItemEntity? = items.find { it.getId() == id }

}