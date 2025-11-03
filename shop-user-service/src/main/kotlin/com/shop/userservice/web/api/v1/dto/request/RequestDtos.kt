package com.shop.userservice.web.api.v1.dto.request

import kotlinx.serialization.Serializable

@Serializable
class UserRequestDto(val username: String, val password: String)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)
