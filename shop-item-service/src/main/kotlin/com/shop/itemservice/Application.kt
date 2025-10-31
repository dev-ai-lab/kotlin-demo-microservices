package com.shop.itemservice

import com.fasterxml.jackson.databind.SerializationFeature
import com.shop.itemservice.config.Health
import com.shop.itemservice.config.HealthStatus
import com.shop.itemservice.config.simpleJwt
import com.shop.itemservice.web.api.v1.routeApiV1
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
@JvmOverloads
fun Application.module() {
    val client = HttpClient(Apache5)

    install(ContentNegotiation) {
        jackson { enable(SerializationFeature.INDENT_OUTPUT) }
    }

    install(io.ktor.server.auth.Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate { cred ->
                cred.payload.getClaim("name").asString()
                    .takeIf { !it.isNullOrBlank() }
                    ?.let(::UserIdPrincipal)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
            }
        }
    }

    routing {
        get("/") { call.respondText("Welcome to shop!", contentType = ContentType.Text.Plain) }
        get("/health") { call.respond(HttpStatusCode.OK, Health(HealthStatus.HEALTHY)) }
        routeApiV1("api/v1")
    }
}