package com.shop.userservice.web.api.v1.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSignupDto(
    val username: String,
    val password: String,
    val firstname: String? = null,
    val lastname: String? = null
)
