package com.shop.orderservice.config.di

import com.shop.orderservice.repository.CartRepository
import com.shop.orderservice.repository.InMemoryCartRepository
import com.shop.orderservice.repository.OrderRepository
import com.shop.orderservice.repository.InMemoryOrderRepository
import com.shop.orderservice.infrastructure.UserServiceGateway
import com.shop.orderservice.infrastructure.ItemServiceGateway
import com.shop.orderservice.service.OrderService
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

fun orderModule(config: ApplicationConfig) = module {
    val userServiceUrl = config.property("shop.shop-order-service.external-services.user-service-url").getString()
    val itemServiceUrl = config.property("shop.shop-order-service.external-services.item-service-url").getString()
    single<CartRepository> { InMemoryCartRepository() }
    single<OrderRepository> { InMemoryOrderRepository() }
    single<UserServiceGateway> { UserServiceGateway(userServiceUrl) }
    single<ItemServiceGateway> { ItemServiceGateway(itemServiceUrl) }
    single<OrderService> { OrderService(get(), get(), get(), get()) }
}
