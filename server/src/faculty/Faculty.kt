package com.somegame.faculty

import com.somegame.responseExceptions.NotFoundException
import faculty.FacultyData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Faculties : IntIdTable() {
    val name = varchar("name", 16)
    val points = integer("points").default(0)
}

class FacultyRepository {
    fun getAllFaculties(): List<Faculty> = transaction {
        Faculty.all().toList()
    }

    fun getFacultyById(id: Int) = transaction {
        Faculty.findById(id)
    }
}

class Faculty(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Faculty>(Faculties)
    var name by Faculties.name
    var points by Faculties.points

    fun getId(): Int = id.value

    fun givePoints(amount: Int) {
        points += amount
    }
}

fun Faculty.publicData() = FacultyData(getId(), name, points)

class FacultyNotFound(id: Int) : NotFoundException("Faculty with id=$id not found")
