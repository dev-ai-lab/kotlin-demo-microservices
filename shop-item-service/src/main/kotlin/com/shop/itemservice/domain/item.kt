package com.shop.itemservice.domain

import java.util.*

/**
 * Represents a product/item in the catalog.
 */
data class Item(
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
