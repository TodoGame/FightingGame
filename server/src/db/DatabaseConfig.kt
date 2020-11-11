package com.somegame.db

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

class DatabaseConfig(private val dbUrl: String, private val dbUser: String, private val dbPassword: String) {
    fun connect() {
        Database.connect(dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)
    }
}
