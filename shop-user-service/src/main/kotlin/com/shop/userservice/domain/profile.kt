package com.shop.userservice.domain

//import com.shop.userservice.service.getPurchasedItems
import java.util.*

data class Profile(
    val userId: UUID,
    var firstname: String,
    var lastname: String,
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
            lastname = "User"
        )
    ).associateBy { it.userId }  // use userId as the key
        .toMutableMap()
)

