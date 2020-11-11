package com.somegame.user.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object UsersTable : IntIdTable() {
    val username = varchar("username", 16).uniqueIndex()
    val password = varchar("password", 50)
    val name = varchar("name", 30)
}
