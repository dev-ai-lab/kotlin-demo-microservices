package com.shop.itemservice.domain


import com.shop.common.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Represents a product/item in the catalog.
 */
@Serializable
data class Item(
    @Serializable(with = UUIDSerializer::class)
    val itemId: UUID,
    val sku: String,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long
)
