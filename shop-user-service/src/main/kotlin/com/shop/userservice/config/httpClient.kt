package com.shop.userservice.config

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.client.engine.apache.*

fun createHttpClient() = HttpClient(Apache) {
    // Intentionally minimal: no client plugins to avoid version-specific resolution issues.
}

val config = ConfigFactory.load()
val itemServiceBaseUrl: String = if (config.hasPath("shop.shop-item-service.baseUrl")) {
    config.getString("shop.shop-item-service.baseUrl")
} else {
    "http://unknown"
}
