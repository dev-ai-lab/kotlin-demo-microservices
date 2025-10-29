package com.shop.userservice.web.api.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.shop.userservice.config.JustSellSession
import com.shop.userservice.config.simpleJwt
import com.shop.userservice.domain.*
import com.shop.userservice.web.api.v1.dto.PurchaseListDto
import com.shop.userservice.web.api.v1.exception.InvalidCredentialException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlin.collections.Map
import kotlin.collections.getOrPut
import kotlin.collections.mapOf
import kotlin.collections.set

fun Route.routeApiV1(path: String) = route(path) {
    post("/login") {
        val userDto = call.receive<UserRequestDto>()
        val user = users.getOrPut(userDto.username) { User(userDto.username, userDto.password) }

        if (user.password != userDto.password) throw InvalidCredentialException("Invalid credentials")
        call.respond(mapOf("token" to simpleJwt.sign(user.username)))
    }

    authenticate {
        post("/profile") {
            val profile = call.receive<Profile>()
            profiles[profile.id] = profile
            call.respond(profiles[profile.id] ?: error("Profile not created"))
        }

        get("/profile/{profile_id}") {
            val profile = profiles.get(call.parameters["profile_id"]) ?: error("No Such Profile")
            call.respond(profile)
        }

        put("/profile/{profile_id}") {
            val purchaseListDto = call.receive<PurchaseListDto>()
            val profileId = call.parameters["profile_id"]
            profiles[profileId]?.purchasedItemsId = purchaseListDto.list
            call.respond(profiles[profileId] ?: error("profile cannot be updated"))
        }
    }
    authenticate("google-oauth") {
        route("/google/login") {
            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                    ?: error("No principal")

                val client = HttpClient(Apache)
                val json = client.get("https://www.googleapis.com/userinfo/v2/me") {
                    header(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
                }.body<String>()
                client.close()

                val data = ObjectMapper().readValue<Map<String, Any?>>(json)
                val id = data["id"] as String?

                if (id != null) {
                    call.sessions.set(JustSellSession(id))
                }
                call.respondRedirect("/")
            }
        }
    }
}
