package com.shop.userservice.domain

import com.shop.common.UUIDSerializer
import com.shop.userservice.config.hashPassword
import java.util.*
import kotlinx.serialization.Serializable

/**
 * Represents a user profile in the system.
 */
@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val passwordHash: String
)

val users = Collections.synchronizedMap(
    listOf(
        User(
            username = "test",
            passwordHash = hashPassword("test")
        )
    ).associateBy { it.id.toString() }.toMutableMap()
)


class UserRequestDto(val username: String, val password: String)
