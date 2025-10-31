package com.shop.userservice.domain

import com.shop.userservice.config.hashPassword
import java.util.*

data class User(
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

