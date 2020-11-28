package com.somegame.items

import item.ItemData

object ItemExtensions {
    fun Item.publicData() = ItemData(id.value, name, price)
}
