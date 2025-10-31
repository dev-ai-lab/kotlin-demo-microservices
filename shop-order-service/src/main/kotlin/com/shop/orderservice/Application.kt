package com.shop.orderservice

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.netty.EngineMain
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json as clientJson
import io.ktor.client.call.body
import io.ktor.server.request.receive
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import com.shop.orderservice.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.shop.orderservice.config.di.orderModule
import org.koin.ktor.plugin.Koin

// Request/Response models
@Serializable
data class CreateCartRequest(val userId: String)
@Serializable
data class AddItemToCartRequest(val userId: String, val itemId: String, val quantity: Int)
@Serializable
data class RemoveItemFromCartRequest(val userId: String, val itemId: String)
@Serializable
data class PlaceOrderRequest(val userId: String)
@Serializable
data class ApiResponse<T>(val success: Boolean, val data: T? = null, val error: String? = null)

// In-memory storage
val carts = ConcurrentHashMap<String, MutableList<OrderItem>>() // userId -> cart items
val orders = ConcurrentHashMap<String, Order>() // orderId -> Order

// Ktor HTTP client for integration
val client = HttpClient(Apache5) {
    install(ClientContentNegotiation) {
        clientJson(Json { ignoreUnknownKeys = true })
    }
}

suspend fun verifyUser(userId: String): Boolean {
    val url = "http://localhost:8080/users/$userId"
    return try {
        val response = client.get(url)
        response.status == HttpStatusCode.OK
    } catch (_: Exception) {
        false
    }
}

suspend fun getItemDetails(itemId: String): ItemDetailsResponse? {
    val url = "http://localhost:8083/items/$itemId"
    return try {
        client.get(url).body<ItemDetailsResponse>()
    } catch (_: Exception) {
        null
    }
}

@Serializable
data class ItemDetailsResponse(val itemId: String, val price: Double, val stock: Int)

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(Koin) {
        modules(orderModule(environment.config))
    }
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = true })
    }
    routing {
        get("/") { call.respondText("Welcome to shop-order-service!", ContentType.Text.Plain) }
        post("/cart") {
            val req = call.receive<CreateCartRequest>()
            if (!verifyUser(req.userId)) {
                call.respond(ApiResponse<String>(false, error = "Invalid userId"))
                return@post
            }
            carts[req.userId] = mutableListOf()
            call.respond(ApiResponse(true, "Cart created for user ${req.userId}"))
        }
        post("/cart/items") {
            val req = call.receive<AddItemToCartRequest>()
            if (!verifyUser(req.userId)) {
                call.respond(ApiResponse<String>(false, error = "Invalid userId"))
                return@post
            }
            val itemDetails = getItemDetails(req.itemId)
            if (itemDetails == null || itemDetails.stock < req.quantity) {
                call.respond(ApiResponse<String>(false, error = "Item unavailable or insufficient stock"))
                return@post
            }
            val cart = carts.getOrPut(req.userId) { mutableListOf() }
            val itemId = UUID.fromString(req.itemId)
            val orderItem = OrderItem(itemId, req.quantity, itemDetails.price)
            cart.add(orderItem)
            call.respond(ApiResponse(true, "Item added to cart"))
        }
        post("/cart/remove") {
            val req = call.receive<RemoveItemFromCartRequest>()
            val cart = carts[req.userId]
            if (cart == null) {
                call.respond(ApiResponse<String>(false, error = "Cart not found"))
                return@post
            }
            cart.removeIf { it.itemId.toString() == req.itemId }
            call.respond(ApiResponse(true, "Item removed from cart"))
        }
        get("/cart/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(ApiResponse<String>(false, error = "Missing userId"))
            val cart = carts[userId] ?: emptyList<OrderItem>()
            call.respond(ApiResponse(true, cart))
        }
        post("/orders") {
            val req = call.receive<PlaceOrderRequest>()
            if (!verifyUser(req.userId)) {
                call.respond(ApiResponse<String>(false, error = "Invalid userId"))
                return@post
            }
            val cart = carts[req.userId] ?: return@post call.respond(ApiResponse<String>(false, error = "Cart not found"))
            if (cart.isEmpty()) {
                call.respond(ApiResponse<String>(false, error = "Cart is empty"))
                return@post
            }
            val orderId = UUID.randomUUID().toString()
            val order = Order(
                orderId = UUID.fromString(orderId),
                userId = UUID.fromString(req.userId),
                items = cart.toList(),
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            orders[orderId] = order
            carts.remove(req.userId)
            call.respond(ApiResponse(true, order))
        }
        get("/orders/{orderId}") {
            val orderId = call.parameters["orderId"] ?: return@get call.respond(ApiResponse<String>(false, error = "Missing orderId"))
            val order = orders[orderId]
            if (order != null) {
                call.respond(ApiResponse(true, order))
            } else {
                call.respond(ApiResponse<String>(false, error = "Order not found"))
            }
        }
        post("/orders/{orderId}/pay") {
            val orderId = call.parameters["orderId"] ?: return@post call.respond(ApiResponse<String>(false, error = "Missing orderId"))
            val order = orders[orderId]
            if (order == null) {
                call.respond(ApiResponse<String>(false, error = "Order not found"))
                return@post
            }
            if (order.status != OrderStatus.PENDING) {
                call.respond(ApiResponse<String>(false, error = "Order not in PENDING status"))
                return@post
            }
            orders[orderId] = order.copy(status = OrderStatus.PAID, updatedAt = System.currentTimeMillis())
            call.respond(ApiResponse(true, "Order marked as PAID"))
        }
    }
}
