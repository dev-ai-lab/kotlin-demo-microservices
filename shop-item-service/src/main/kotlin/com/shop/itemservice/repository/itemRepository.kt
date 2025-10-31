package com.shop.itemservice.repository

import com.shop.itemservice.domain.Item
import java.util.*

interface ItemRepository {
    fun getItemById(id: Int): Item?
    fun getAllItems(): List<Item>
}

class InMemoryItemRepository : ItemRepository {
    private val items = mapOf(
        1 to Item(
            UUID.randomUUID(),
            "DELL Monitor",
            "High quality monitor",
            "",
            10.0,
            100,
            "Electronics",
            System.currentTimeMillis(),
            System.currentTimeMillis()
        ),
        2 to Item(
            UUID.randomUUID(),
            "IPhone II",
            "Latest iPhone model",
            "",
            999.99,
            50,
            "Electronics",
            System.currentTimeMillis(),
            System.currentTimeMillis()
        ),
        3 to Item(
            UUID.randomUUID(),
            "Camera Tripod",
            "Stable tripod for cameras",
            "",
            49.99,
            20,
            "Photography",
            System.currentTimeMillis(),
            System.currentTimeMillis()
        ),
        4 to Item(
            UUID.randomUUID(),
            "Alpha 7 Camera",
            "Professional camera",
            "",
            20.99,
            15,
            "Photography",
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )
    )
    override fun getItemById(id: Int): Item? = items[id]
    override fun getAllItems(): List<Item> = items.values.toList()
}
