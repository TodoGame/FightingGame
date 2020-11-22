package com.somegame.db

import com.somegame.items.tables.Items
import com.somegame.user.tables.UserItems
import com.somegame.user.tables.Users
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class DatabaseConfig(private val dbUrl: String) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun configure() {
        connect()
        createTables()
    }

    private fun connect() {
        Database.connect(dbUrl, driver = "org.postgresql.Driver")
        logger.info("Connected to database $dbUrl")
    }

    private fun createTables() {
        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Items)
            SchemaUtils.create(UserItems)
        }
    }
}
