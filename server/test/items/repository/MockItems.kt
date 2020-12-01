package com.somegame.items.repository

import com.somegame.items.Item
import com.somegame.items.ItemRepository
import com.somegame.items.publicData
import io.mockk.every
import io.mockk.mockk

val testItem1 = createMockItem(1, "Bat", 1)
val testItem2 = createMockItem(2, "Sword", 10)

fun createMockItem(id: Int, name: String, price: Int): Item {
    val item = mockk<Item>()

    every { item.getId() } returns id
    every { item.name } returns name
    every { item.price } returns price

    return item
}

fun createMockItemRepository(): ItemRepository {
    val itemRepository = mockk<ItemRepository>()

    val items = listOf(testItem1, testItem2)

    every { itemRepository.getItemById(any()) } answers {
        val id = firstArg<Int>()
        items.find { it.getId() == id }
    }

    every { itemRepository.getAllItems() } returns items

    return itemRepository
}