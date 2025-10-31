package com.shop.paymentservice.infrastructure.orderservice

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class OrderServiceGateway(private val baseUrl: String) {
    private val client = HttpClient(Apache5) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    suspend fun verifyOrder(orderId: String): Boolean {
        val url = "$baseUrl/orders/$orderId"
        return try {
            val response = client.get(url)
            response.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }
    }
}