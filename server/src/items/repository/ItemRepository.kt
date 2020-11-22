package com.somegame.items.repository

interface ItemRepository {
    fun getItemById(id: Int): ItemEntity?
}