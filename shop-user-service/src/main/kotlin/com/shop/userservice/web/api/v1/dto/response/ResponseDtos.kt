package com.shop.userservice.web.api.v1.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UserSignupResponseDto(
    val username: String,
    val profile: ProfileInfo
)

@Serializable
data class ProfileInfo(
    val firstname: String,
    val lastname: String
)

@Serializable
data class AuthResponse(val token: String, val refresh_token: String? = null)
