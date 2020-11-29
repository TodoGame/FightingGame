package com.somegame.faculty

import faculty.FacultyData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Faculty(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Faculty>(Faculties)

    var name by Faculties.name
}

fun Faculty.publicData() = FacultyData(name)
