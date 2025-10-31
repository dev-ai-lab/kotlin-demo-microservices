package com.shop.orderservice.domain

import java.util.*

/**
 * Represents an item in an order or cart.
 */
data class OrderItem(
    val itemId: UUID,
    val quantity: Int,
    val price: Double
)

/**
 * Represents an order or cart.
 */
data class Order(
    val orderId: UUID,
    val userId: UUID,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val createdAt: Long,
    val updatedAt: Long
)

enum class OrderStatus {
    CART, PENDING, PAID, SHIPPED, CANCELLED
}

