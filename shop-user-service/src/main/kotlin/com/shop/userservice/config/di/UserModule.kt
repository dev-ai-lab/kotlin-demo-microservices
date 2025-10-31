package com.shop.userservice.config.di

import com.shop.userservice.config.SimpleJWT
import io.ktor.server.config.*
import org.koin.dsl.module

fun userModule(config: ApplicationConfig) = module {
    single {
        val jwtSecret = config.property("shop.shop-user-service.jwt.secret").getString()
        SimpleJWT(jwtSecret)
    }
}
