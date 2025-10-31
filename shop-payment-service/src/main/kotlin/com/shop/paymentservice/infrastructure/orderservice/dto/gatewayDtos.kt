package com.shop.paymentservice.infrastructure.orderservice.dto

import kotlinx.serialization.Serializable

@Serializable
data class InitiatePaymentRequest(val orderId: String, val userId: String, val amount: Double)
@Serializable
data class ApiResponse<T>(val success: Boolean, val data: T? = null, val error: String? = null)