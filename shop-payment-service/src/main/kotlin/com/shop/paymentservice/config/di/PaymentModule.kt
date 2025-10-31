package com.shop.paymentservice.config.di

import com.shop.paymentservice.repository.PaymentRepository
import com.shop.paymentservice.service.PaymentService
import com.shop.paymentservice.infrastructure.orderservice.OrderServiceGateway
import org.koin.dsl.module
import io.ktor.server.config.ApplicationConfig

fun paymentModule(config: ApplicationConfig) = module {
    val orderServiceUrl = config.property("shop.shop-payment-service.external-services.order-service-url").getString()
    single { PaymentRepository() }
    single { OrderServiceGateway(orderServiceUrl) }
    single { PaymentService(get(), get()) }
}
