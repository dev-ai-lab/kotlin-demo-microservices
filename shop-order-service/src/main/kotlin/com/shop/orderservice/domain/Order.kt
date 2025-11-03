package com.shop.orderservice.domain

import com.shop.common.UUIDSerializer
import java.util.*
import kotlinx.serialization.Serializable

/**
 * Represents an item in an order or cart.
 */
@Serializable
data class OrderItem(
    @Serializable(with = UUIDSerializer::class)
    val itemId: UUID,
    val quantity: Int,
    val price: Double
)

/**
 * Represents an order or cart.
 */
@Serializable
data class Order(
    @Serializable(with = UUIDSerializer::class)
    val orderId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val createdAt: Long,
    val updatedAt: Long
)

enum class OrderStatus {
    CART, PENDING, PAID, SHIPPED, CANCELLED
}
