package com.somegame.items

import item.ItemData

object ItemExtensions {
    fun Item.publicData() = ItemData(getId(), name, price)
}
