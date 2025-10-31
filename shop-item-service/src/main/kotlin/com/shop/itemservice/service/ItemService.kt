package com.shop.itemservice.service

import com.shop.itemservice.domain.Item
import com.shop.itemservice.repository.ItemRepository

class ItemService(private val repo: ItemRepository) {
    fun getItemById(id: Int): Item? = repo.getItemById(id)
    fun getAllItems(): List<Item> = repo.getAllItems()
}

