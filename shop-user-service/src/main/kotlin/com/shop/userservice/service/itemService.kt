package com.shop.userservice.service

import com.shop.userservice.config.createHttpClient
import com.shop.userservice.config.itemServiceBaseUrl
import com.shop.userservice.domain.Item
import com.shop.userservice.domain.Profile
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("HttpClient_logger")!!

suspend fun getPurchasedItems(userProfile: Profile): List<Item> {
    logger.info("Call to item-service started")
    val client = createHttpClient()
    try {
        val purchasedItems = userProfile.purchasedItemsId?.map { id ->
            client.get("${itemServiceBaseUrl}/api/v1/item/$id").body<Item>()
        } ?: listOf()
        logger.info("Call to item-service ended")
        return purchasedItems
    } finally {
        client.close()
    }
}
