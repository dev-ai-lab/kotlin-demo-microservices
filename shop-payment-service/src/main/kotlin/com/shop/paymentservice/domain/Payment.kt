package com.shop.paymentservice.domain

import java.util.*

/**
 * Represents a payment transaction.
 */
data class Payment(
    val paymentId: UUID,
    val orderId: UUID,
    val userId: UUID,
    val amount: Double,
    val status: PaymentStatus,
    val createdAt: Long,
    val updatedAt: Long
)

enum class PaymentStatus {
    INITIATED, COMPLETED, FAILED, REFUNDED
}

