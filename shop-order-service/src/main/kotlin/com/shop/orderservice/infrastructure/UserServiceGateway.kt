package com.shop.orderservice.infrastructure

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.*

class UserServiceGateway(private val baseUrl: String) {
    private val client = HttpClient(Apache5) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    suspend fun verifyUser(userId: String): Boolean {
        val url = "$baseUrl/users/$userId"
        return try {
            val response = client.get(url)
            response.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }
    }
}
