package com.shop.userservice.web.api.v1.dto

data class ProfileCreateDto(
    val firstname: String,
    val lastname: String,
    val purchasedItemsId: List<Int>? = null
)
