package com.somegame.items

import org.jetbrains.exposed.dao.id.IntIdTable

object Items : IntIdTable() {
    val name = varchar("name", 30)
    val price = integer("price")
}
