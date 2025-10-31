package com.shop.orderservice.repository

import com.shop.orderservice.domain.OrderItem

interface CartRepository {
    fun getCart(userId: String): List<OrderItem>
    fun createCart(userId: String)
    fun addItem(userId: String, item: OrderItem)
    fun removeItem(userId: String, itemId: String)
    fun clearCart(userId: String)
}

class InMemoryCartRepository : CartRepository {
    private val carts = mutableMapOf<String, MutableList<OrderItem>>()
    override fun getCart(userId: String): List<OrderItem> = carts[userId] ?: emptyList()
    override fun createCart(userId: String) { carts[userId] = mutableListOf() }
    override fun addItem(userId: String, item: OrderItem) { carts.getOrPut(userId) { mutableListOf() }.add(item) }
    override fun removeItem(userId: String, itemId: String) { carts[userId]?.removeIf { it.itemId.toString() == itemId } }
    override fun clearCart(userId: String) { carts.remove(userId) }
}

