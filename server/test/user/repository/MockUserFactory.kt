package com.somegame.user.repository

import com.somegame.items.Item
import com.somegame.security.UserPrincipal
import com.somegame.user.User
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.sql.SizedCollection
import user.UserData
import user.Username

object MockUserFactory {
    fun create(username: Username, password: String, name: String): User {
        val user = mockk<User>()

        every { user.username } returns username
        every { user.password } returns password
        every { user.name } returns name

        every { user.getPublicData() } returns UserData(username, name, listOf())
        every { user.getPrincipal() } returns UserPrincipal(username)

        var inventory = SizedCollection<Item>()

        every { user.inventory } returns inventory

        every { user.addToInventory(any()) } answers {
            val item = firstArg<Item>()
            if (item in inventory) {
                throw User.ItemAlreadyInInventoryException(item)
            } else {
                inventory = SizedCollection(inventory + listOf(item))
            }
        }

        return user
    }
}
