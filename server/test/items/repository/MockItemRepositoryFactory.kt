package com.somegame.items.repository

import com.somegame.items.ItemRepository
import io.mockk.every
import io.mockk.mockk
import items.repository.MockItemFactory

object MockItemRepositoryFactory {
    val bat = MockItemFactory.create(1, "Bat", 1)
    val sword = MockItemFactory.create(2, "Sword", 10)

    val items = listOf(bat, sword)

    fun create(): ItemRepository {
        val itemRepository = mockk<ItemRepository>()

        every { itemRepository.getItemById(any()) } answers {
            val id = firstArg<Int>()
            if (id in items.indices) {
                items[id]
            } else {
                null
            }
        }

        return itemRepository
    }


}