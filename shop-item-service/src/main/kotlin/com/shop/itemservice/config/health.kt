package com.shop.itemservice.config

import kotlinx.serialization.Serializable

@Serializable
data class Health(val health: HealthStatus)

enum class HealthStatus{
    HEALTHY,
    OVERLOADED,
}