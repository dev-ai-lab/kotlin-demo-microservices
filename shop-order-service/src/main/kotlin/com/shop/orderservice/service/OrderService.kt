package com.shop.orderservice.service

import com.shop.orderservice.domain.*
import com.shop.orderservice.repository.CartRepository
import com.shop.orderservice.repository.OrderRepository
import com.shop.orderservice.infrastructure.UserServiceGateway
import com.shop.orderservice.infrastructure.ItemServiceGateway
import java.util.*

class OrderService(
    private val cartRepo: CartRepository,
    private val orderRepo: OrderRepository,
    private val userGateway: UserServiceGateway,
    private val itemGateway: ItemServiceGateway
) {
    suspend fun createCart(userId: String): Boolean {
        if (!userGateway.verifyUser(userId)) return false
        cartRepo.createCart(userId)
        return true
    }
    suspend fun addItemToCart(userId: String, itemId: String, quantity: Int): String? {
        if (!userGateway.verifyUser(userId)) return "Invalid userId"
        val itemDetails = itemGateway.getItemDetails(itemId)
        if (itemDetails == null || itemDetails.stock < quantity) return "Item unavailable or insufficient stock"
        val orderItem = OrderItem(UUID.fromString(itemId), quantity, itemDetails.price)
        cartRepo.addItem(userId, orderItem)
        return null
    }
    fun removeItemFromCart(userId: String, itemId: String): Boolean {
        cartRepo.removeItem(userId, itemId)
        return true
    }
    fun getCart(userId: String): List<OrderItem> = cartRepo.getCart(userId)
    suspend fun placeOrder(userId: String): Order? {
        if (!userGateway.verifyUser(userId)) return null
        val cart = cartRepo.getCart(userId)
        if (cart.isEmpty()) return null
        val orderId = UUID.randomUUID().toString()
        val order = Order(
            orderId = UUID.fromString(orderId),
            userId = UUID.fromString(userId),
            items = cart.toList(),
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        orderRepo.save(order)
        cartRepo.clearCart(userId)
        return order
    }
    fun getOrder(orderId: String): Order? = orderRepo.findById(orderId)
}
