package com.somegame.db

import com.somegame.faculty.Faculties
import com.somegame.faculty.Faculty
import com.somegame.items.Items
import com.somegame.items.UserItems
import com.somegame.user.Users
import faculty.FixedFaculties
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
            SchemaUtils.createMissingTablesAndColumns(Users, Items, UserItems, Faculties)
            addFaculties()
        }
    }

    private fun addFaculties() {
        for (faculty in FixedFaculties.values()) {
            if (Faculty.findById(faculty.id) == null) {
                Faculty.new(faculty.id) {
                    name = faculty.name
                }
            }
        }
    }
}
