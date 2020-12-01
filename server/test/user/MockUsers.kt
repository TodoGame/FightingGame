package com.somegame.user

import com.somegame.faculty.Faculty
import com.somegame.items.Item
import com.somegame.items.publicData
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.jetbrains.exposed.sql.SizedCollection
import testFaculty1
import testFaculty2
import user.Username

fun createMockUser(username: Username, password: String, name: String, faculty: Faculty): User {
    val user = mockk<User>()

    every { user.username } returns username
    every { user.password } returns password
    every { user.name } returns name
//    every { user.faculty } returns faculty
    every { user.loadFaculty() } returns faculty

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

fun createMockUserRepository(): UserRepository {
    val userRepository = mockk<UserRepository>()

    val user1 = createMockUser("user1", "pass1", "User1", testFaculty1)
    val user2 = createMockUser("user2", "pass2", "User2", testFaculty2)

    val users = mutableListOf(user1, user2)

    every { userRepository.findUserByUsername(any()) } answers { users.find { it.username == firstArg<Username>() } }
    every { userRepository.createUser(any(), any(), any(), any()) } answers {
        createMockUser(arg(0), arg(1), arg(2), arg(3)).also {
            users.add(it)
        }
    }

    return userRepository
}

fun UserRepository.makeNewTestUser(username: Username): User {
    return createUser(username, "testPassword", username.capitalize(), testFaculty1)
}

fun UserRepository.user1() = findUserByUsername("user1")!!

fun UserRepository.user2() = findUserByUsername("user2")!!
