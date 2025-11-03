package com.shop.itemservice.web.api.v1

import com.shop.itemservice.service.ItemService
import com.shop.itemservice.web.api.v1.dto.ErrorResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.routeApiV1(path: String) = route(path) {
    val itemService by inject<ItemService>()

    get("/item/{item_id}") {
        try {
            val itemId = call.parameters["item_id"]?.toInt() ?: throw IllegalStateException("wrong id")
            val item = itemService.getItemById(itemId) ?: throw IllegalStateException("No Such Item")
            call.respond(item)
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = e.message ?: ""))
        }
    }

    authenticate {
        // Add authenticated routes here
    }
}