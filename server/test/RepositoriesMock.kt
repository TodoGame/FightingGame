package com.somegame

import com.somegame.faculty.Faculty
import com.somegame.faculty.FacultyRepository
import com.somegame.items.Item
import com.somegame.items.ItemRepository
import com.somegame.items.publicData
import com.somegame.user.ItemAlreadyInInventoryException
import com.somegame.user.User
import com.somegame.user.UserRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import item.ItemType
import org.jetbrains.exposed.sql.SizedCollection
import org.koin.dsl.module
import user.Username

class RepositoriesMock {
    private val items = mutableListOf<Item>()
    private val faculties = mutableListOf<Faculty>()
    private val users = mutableListOf<User>()

    val itemRepository = createMockItemRepository()
    val facultyRepository = createMockFacultyRepository()
    val userRepository = createMockUserRepository()

    val repositoriesModule = module {
        single { itemRepository }
        single { facultyRepository }
        single { userRepository }
    }

    fun clear() {
        items.clear()
        items.addAll(
            listOf(
                createMockItem(1, ItemType.MainWeapon, "Bat", 30, 20),
                createMockItem(2, ItemType.MainWeapon, "Sword", 100, 40),
                createMockItem(3, ItemType.Additional, "Banana", 10, -40),
                createMockItem(4, ItemType.Additional, "Dice", 150, 1000)
            )
        )
        faculties.clear()
        faculties.addAll(
            listOf(
                createMockFaculty(1, "MM"),
                createMockFaculty(2, "PMPU")
            )
        )
        users.clear()
        users.addAll(
            listOf(
                createMockUser("user1", "pass1", "User1", faculties[0]),
                createMockUser("user2", "pass2", "User2", faculties[1])
            )
        )
    }

    fun makeNewTestUser(username: Username, facultyId: Int = 1) =
        userRepository.createUser(username, "testPassword", username.capitalize(), faculties[facultyId - 1])

    fun createMockFaculty(id: Int, name: String): Faculty {
        val faculty = mockk<Faculty>()
        var points = 0
        every { faculty.getId() } returns id
        every { faculty.name } returns name
        every { faculty.points } answers { points }
        every { faculty.givePoints(any()) } answers {
            points += firstArg<Int>()
        }
        return faculty
    }

    fun createMockFacultyRepository(): FacultyRepository {
        val facultyRepository = mockk<FacultyRepository>()

        every { facultyRepository.getAllFaculties() } returns faculties
        every { facultyRepository.getFacultyById(any()) } answers {
            faculties.find { it.getId() == firstArg() }
        }

        return facultyRepository
    }

    fun createMockItem(id: Int, type: ItemType, name: String, price: Int, damage: Int): Item {
        val item = mockk<Item>()

        every { item.getId() } returns id
        every { item.type } returns type
        every { item.name } returns name
        every { item.price } returns price
        every { item.damage } returns damage

        return item
    }

    fun createMockItemRepository(): ItemRepository {
        val itemRepository = mockk<ItemRepository>()

        every { itemRepository.getItemById(any()) } answers {
            val id = firstArg<Int>()
            items.find { it.getId() == id }
        }

        every { itemRepository.getAllItems() } returns items

        return itemRepository
    }

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

        every { userRepository.findUserByUsername(any()) } answers { users.find { it.username == firstArg<Username>() } }
        every { userRepository.createUser(any(), any(), any(), any()) } answers {
            createMockUser(arg(0), arg(1), arg(2), arg(3)).also {
                users.add(it)
            }
        }

        return userRepository
    }
}

fun FacultyRepository.testFaculty1() = getFacultyById(1)!!

fun FacultyRepository.testFaculty2() = getFacultyById(2)!!

fun UserRepository.user1() = findUserByUsername("user1")!!

fun UserRepository.user2() = findUserByUsername("user2")!!

fun ItemRepository.testItem1() = getItemById(1)!!

fun ItemRepository.testItem2() = getItemById(2)!!
