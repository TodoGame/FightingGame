package com.somegame.faculty

import org.jetbrains.exposed.sql.transactions.transaction

class FacultyRepository {
    fun getAllFaculties(): List<Faculty> = transaction {
        Faculty.all().toList()
    }

    fun getFacultyById(id: Int) = transaction {
        Faculty.findById(id)
    }
}
