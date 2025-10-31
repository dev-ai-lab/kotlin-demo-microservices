package com.shop.paymentservice.repository

import com.shop.paymentservice.domain.Payment

class PaymentRepository {
    private val payments = mutableMapOf<String, Payment>()
    fun save(payment: Payment) { payments[payment.paymentId.toString()] = payment }
    fun findById(paymentId: String): Payment? = payments[paymentId]
}