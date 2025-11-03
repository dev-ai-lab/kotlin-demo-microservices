package com.shop.paymentservice

import com.shop.paymentservice.config.di.paymentModule
import com.shop.paymentservice.infrastructure.orderservice.dto.ApiResponse
import com.shop.paymentservice.infrastructure.orderservice.dto.InitiatePaymentRequest
import com.shop.paymentservice.service.PaymentService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(Koin) {
        modules(paymentModule(environment.config))
    }
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = true })
    }
    val paymentService = get<PaymentService>()
    routing {
        get("/") { call.respondText("Welcome to shop-payment-service!", ContentType.Text.Plain) }
        post("/payments") {
            val req = call.receive<InitiatePaymentRequest>()
            val result = paymentService.processPayment(req)
            call.respond(result)
        }

        get("/payments/{paymentId}") {
            val paymentId = call.parameters["paymentId"] ?: return@get call.respond(ApiResponse<String>(false, error = "Missing paymentId"))
            val payment = paymentService.getPayment(paymentId)
            if (payment != null) {
                call.respond(ApiResponse(true, payment))
            } else {
                call.respond(ApiResponse<String>(false, error = "Payment not found"))
            }
        }
    }
}