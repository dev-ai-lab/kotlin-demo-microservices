package com.shop.orderservice.infrastructure

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetailsResponse(val itemId: String, val price: Double, val stock: Int)

class ItemServiceGateway(private val baseUrl: String) {
    private val client = HttpClient(Apache5) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    suspend fun getItemDetails(itemId: String): ItemDetailsResponse? {
        val url = "$baseUrl/items/$itemId"
        return try {
            client.get(url).body<ItemDetailsResponse>()
        } catch (_: Exception) {
            null
        }
    }
}

