package com.somegame.user.tables

import com.somegame.items.Items
import org.jetbrains.exposed.dao.id.IntIdTable

object UserItems : IntIdTable() {
    val item = reference("item", Items)
    val user = reference("user", Users)

    override val primaryKey = PrimaryKey(item, user)
}
