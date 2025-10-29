package com.shop.itemservice.web.api.v1

import com.shop.itemservice.config.simpleJwt
import com.shop.itemservice.domain.LoginRegister
import com.shop.itemservice.domain.User
import com.shop.itemservice.domain.users
import com.shop.itemservice.repository.shopItems
import com.shop.itemservice.web.api.v1.exception.InvalidCredentialException
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.http.HttpStatusCode

fun Route.routeApiV1(path: String) = route(path) {
    post("/login") {
        try {
            val userDto = call.receive<LoginRegister>()
            val user = users.getOrPut(userDto.username) {
                User(userDto.username, userDto.password)
            }
            if (user.password != userDto.password) throw InvalidCredentialException("Invalid credentials")

            call.respond(mapOf("token" to simpleJwt.sign(user.username)))
        } catch (e: InvalidCredentialException) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("Ok" to false, "error" to (e.message ?: "")))
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "")))
        }
    }

    get("/item/{item_id}") {
        try {
            val itemId = call.parameters["item_id"]?.toInt() ?: throw IllegalStateException("wrong id")
            val item = shopItems[itemId] ?: throw IllegalStateException("No Such Item")
            call.respond(item)
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "")))
        }
    }

    authenticate {
        // Add authenticated routes here
    }
}