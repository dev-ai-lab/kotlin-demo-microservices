package com.shop.itemservice.web.api.v1.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)