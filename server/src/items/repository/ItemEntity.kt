package com.somegame.items.repository

interface ItemEntity {
    val name: String
    val price: Int

    fun getId(): Int
}