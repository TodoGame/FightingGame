package com.somegame.faculty

import org.jetbrains.exposed.dao.id.IntIdTable

object Faculties : IntIdTable() {
    val name = varchar("name", 16)
}
