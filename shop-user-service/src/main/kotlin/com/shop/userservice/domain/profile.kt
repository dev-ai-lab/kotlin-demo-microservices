package com.shop.userservice.domain

import com.shop.common.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Profile(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    var firstname: String,
    var lastname: String,
    val email: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    /**
     * Fetch purchased items for this profile.
     * This is a suspend function because it performs an HTTP client call.
     */
    //suspend fun fetchPurchasedItems(): List<Item> = getPurchasedItems(this)
}

val profiles = Collections.synchronizedMap(
    listOf(
        Profile(
            userId = UUID.randomUUID(),   // link to the 'test' user in users map
            firstname = "Test",
            lastname = "User",
            email = "user@mail.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    ).associateBy { it.userId }  // use userId as the key
        .toMutableMap()
)
