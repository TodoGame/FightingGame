package com.somegame.user.repository

import com.somegame.items.Item
import com.somegame.items.ItemExtensions.publicData
import com.somegame.user.User
import com.somegame.user.UserExtensions
import com.somegame.user.UserExtensions.publicData
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.sql.SizedCollection
import user.Username

object MockUserFactory {
    fun create(username: Username, password: String, name: String): User {
        val user = mockk<User>()

        every { user.username } returns username
        every { user.password } returns password
        every { user.name } returns name
        every { user.money } returns 0

        var inventory = SizedCollection<Item>()

        every { user.inventory } returns inventory

        every { user.publicInventory() } returns inventory.map { it.publicData() }

        every { user.addToInventory(any()) } answers {
            val item = firstArg<Item>()
            if (item in inventory) {
                throw UserExtensions.ItemAlreadyInInventoryException(item)
            } else {
                inventory = SizedCollection(inventory + listOf(item))
            }
        }

        return user
    }
}
