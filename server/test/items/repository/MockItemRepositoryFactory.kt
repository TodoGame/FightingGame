package com.somegame.items.repository

import com.somegame.items.ItemRepository
import io.mockk.every
import io.mockk.mockk
import items.repository.MockItemFactory

object MockItemRepositoryFactory {

    fun create(): ItemRepository {
        val itemRepository = mockk<ItemRepository>()

        val bat = MockItemFactory.create(1, "Bat", 1)
        val sword = MockItemFactory.create(2, "Sword", 10)

        val items = listOf(bat, sword)

        every { itemRepository.getItemById(any()) } answers {
            val id = firstArg<Int>()
            items.find { it.getId() == id }
        }

        return itemRepository
    }
}
