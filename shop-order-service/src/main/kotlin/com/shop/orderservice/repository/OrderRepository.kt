package com.shop.orderservice.repository

import com.shop.orderservice.domain.Order

interface OrderRepository {
    fun save(order: Order)
    fun findById(orderId: String): Order?
}

class InMemoryOrderRepository : OrderRepository {
    private val orders = mutableMapOf<String, Order>()
    override fun save(order: Order) { orders[order.orderId.toString()] = order }
    override fun findById(orderId: String): Order? = orders[orderId]
}

