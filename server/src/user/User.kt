package com.somegame.user

import com.somegame.faculty.Faculties
import com.somegame.faculty.Faculty
import com.somegame.faculty.publicData
import com.somegame.items.Item
import com.somegame.items.UserItems
import com.somegame.items.publicData
import com.somegame.security.UserPrincipal
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.KoinComponent
import user.UserData
import user.Username

val USER_MAX_USERNAME_LENGTH = 16
val USER_MAX_PASSWORD_LENGTH = 100
val USER_MAX_NAME_LENGTH = 30

object Users : IntIdTable() {
    val username = varchar("username", USER_MAX_USERNAME_LENGTH).uniqueIndex()
    val password = varchar("password", USER_MAX_PASSWORD_LENGTH)
    val name = varchar("name", USER_MAX_NAME_LENGTH)

    val faculty = reference("faculty", Faculties)

    val money = integer("money").default(0)
}

class UserRepository : KoinComponent {
    fun findUserByUsername(username: Username): User? {
        return transaction {
            val result = User.find { Users.username eq username }
            if (result.count() == 0L) {
                null
            } else {
                result.first()
            }
        }
    }

    fun createUser(username: Username, password: String, name: String, faculty: Faculty): User = transaction {
        User.new {
            this.username = username
            this.password = password
            this.name = name
            this.faculty = faculty
        }
    }
}

fun UserRepository.doesUserExist(username: Username): Boolean = findUserByUsername(username) != null

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var username: Username by Users.username

    var password by Users.password

    var name by Users.name

    var faculty by Faculty referencedOn Users.faculty

    fun loadFaculty(): Faculty = transaction { faculty }

    var inventory by Item via UserItems

    var money by Users.money

    fun hasItem(item: Item) = transaction {
        inventory.find { it.id == item.id } != null
    }

    fun addToInventory(item: Item) = transaction {
        inventory = SizedCollection(inventory + listOf(item))
    }

    fun acceptMoney(amount: Int) = transaction {
        money += amount
    }

    fun spendMoney(amount: Int) = transaction {
        money -= amount
    }

    fun publicInventory() = transaction { inventory.map { it.publicData() } }
    override fun toString() = "User(id=$id, username=$username)"
}

fun User.buyItem(item: Item) {
    if (hasItem(item)) {
        throw ItemAlreadyInInventoryException(item)
    }
    if (money >= item.price) {
        addToInventory(item)
        spendMoney(item.price)
    } else {
        throw NotEnoughMoneyException(item)
    }
}

fun User.awardPointsToUserAndFaculty(amount: Int) {
    acceptMoney(amount)
    loadFaculty().givePoints(amount)
}

class NotEnoughMoneyException(item: Item) : IllegalStateException("Not enough money to buy ${item.publicData()}")

class ItemAlreadyInInventoryException(item: Item) :
    IllegalArgumentException("Item ${item.publicData()} already in inventory")

fun User.principal() = UserPrincipal(username)

fun User.publicData() = UserData(username, name, publicInventory(), money, loadFaculty().publicData())
