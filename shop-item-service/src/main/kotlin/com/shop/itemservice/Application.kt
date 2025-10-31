package com.shop.itemservice

import com.shop.itemservice.config.Health
import com.shop.itemservice.config.HealthStatus
import com.shop.itemservice.config.di.itemModule
import com.shop.itemservice.config.simpleJwt
import com.shop.itemservice.web.api.v1.routeApiV1
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        modules(itemModule(environment.config))
    }

    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = true }) // use json serialization if you needed native
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