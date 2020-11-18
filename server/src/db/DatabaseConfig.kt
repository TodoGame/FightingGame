package com.somegame.db

import com.somegame.user.tables.Users
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConfig(private val dbUrl: String, private val dbUser: String, private val dbPassword: String) {
    fun configure() {
        connect()
        createTables()
    }
    private fun connect() {
        Database.connect(dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)
    }
    private fun createTables() {
        transaction {
            SchemaUtils.create(Users)
        }
    }
}
