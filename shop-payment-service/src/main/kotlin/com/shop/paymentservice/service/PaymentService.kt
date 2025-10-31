package com.shop.paymentservice.service

import com.shop.paymentservice.domain.Payment
import com.shop.paymentservice.domain.PaymentStatus
import com.shop.paymentservice.infrastructure.orderservice.OrderServiceGateway
import com.shop.paymentservice.infrastructure.orderservice.dto.ApiResponse
import com.shop.paymentservice.infrastructure.orderservice.dto.InitiatePaymentRequest
import com.shop.paymentservice.repository.PaymentRepository
import java.util.UUID

// --- PaymentService.kt ---
class PaymentService(
    private val repo: PaymentRepository,
    private val client: OrderServiceGateway
) {
    suspend fun processPayment(req: InitiatePaymentRequest): ApiResponse<Payment> {
        if (!client.verifyOrder(req.orderId)) {
            return ApiResponse(false, error = "Invalid orderId")
        }
        val paymentId = UUID.randomUUID().toString()
        val payment = Payment(
            paymentId = UUID.fromString(paymentId),
            orderId = UUID.fromString(req.orderId),
            userId = UUID.fromString(req.userId),
            amount = req.amount,
            status = PaymentStatus.COMPLETED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repo.save(payment)
        // TODO: Call Order Service to update order status to PAID
        return ApiResponse(true, payment)
    }

    fun getPayment(paymentId: String): Payment? = repo.findById(paymentId)
}