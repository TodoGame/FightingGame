package com.somegame.user.repository

import com.somegame.items.Item
import com.somegame.items.publicData
import com.somegame.user.ItemAlreadyInInventoryException
import com.somegame.user.User
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.jetbrains.exposed.sql.SizedCollection
import user.Username

object MockUserFactory {
    fun create(username: Username, password: String, name: String): User {
        val user = mockk<User>()

        every { user.username } returns username
        every { user.password } returns password
        every { user.name } returns name

        val givenMoney = mutableListOf<Int>()

        every { user.acceptMoney(capture(givenMoney)) } just Runs
        every { user.spendMoney(any()) } answers {
            givenMoney.add(-1 * firstArg<Int>())
        }
        every { user.money } answers { givenMoney.sum() }

        var inventory = SizedCollection<Item>()

        every { user.inventory } returns inventory
        every { user.hasItem(any()) } answers { firstArg() in inventory }

        every { user.publicInventory() } returns inventory.map { it.publicData() }

        every { user.addToInventory(any()) } answers {
            val item = firstArg<Item>()
            if (item in inventory) {
                throw ItemAlreadyInInventoryException(item)
            } else {
                inventory = SizedCollection(inventory + listOf(item))
            }
        }

        return user
    }
}
