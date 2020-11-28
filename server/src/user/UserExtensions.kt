package com.somegame.user

import com.somegame.items.Item
import com.somegame.items.ItemExtensions.publicData
import com.somegame.security.UserPrincipal
import user.UserData

object UserExtensions {
    fun User.buyItem(item: Item) {
        if (hasItem(item)) {
            throw ItemAlreadyInInventoryException(item)
        }
        if (money >= item.price) {
            addToInventory(item)
            spendMoney(item.price)
        } else {
            throw NotEnoughMoneyException(item)
        }
    }

    fun User.principal() = UserPrincipal(username)

    fun User.publicData() = UserData(username, name, publicInventory(), money)

    class NotEnoughMoneyException(item: Item) : IllegalStateException("Not enough money to buy ${item.publicData()}")

    class ItemAlreadyInInventoryException(item: Item) :
        IllegalArgumentException("Item ${item.publicData()} already in inventory")
}
