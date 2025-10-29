/*
package com.shop.itemservice

import com.fasterxml.jackson.databind.SerializationFeature
import com.shop.itemservice.config.Health
import com.shop.itemservice.config.HealthStatus
import com.shop.itemservice.config.simpleJwt
import com.shop.itemservice.web.api.v1.routeApiV1
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*

import io.ktor.serialization.jackson.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

*/
/**
 *
fun main(args: Array<String>) {
    embeddedServer(
    Netty,
    watchPaths = listOf("solutions/exercise4"),
    port = 8080,
    module = Application::mymodule
    ).apply { start(wait = true) }
}
 *//*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {
    val client = HttpClient(Apache) {
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    routing {
        get("/") {
            call.respondText("Welcome to shop!", contentType = ContentType.Text.Plain)
        }

        get("/health") {
            call.response.status(HttpStatusCode.OK)
            call.respond(Health(HealthStatus.HEALTHY))
        }
        // route to main api
        routeApiV1("api/v1")
    }
}*/


package com.shop.itemservice

import com.fasterxml.jackson.databind.SerializationFeature
import com.shop.itemservice.config.Health
import com.shop.itemservice.config.HealthStatus
import com.shop.itemservice.config.simpleJwt
import com.shop.itemservice.web.api.v1.routeApiV1
import io.ktor.client.*
import io.ktor.client.engine.apache5.* // updated engine for Ktor 3.x
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module() {
    // Modern HttpClient (optional)
    val client = HttpClient(Apache5) {
        // configure timeouts, logging, etc. if needed
    }

    // JSON serialization
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    // JWT authentication
    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate { credential ->
                val name = credential.payload.getClaim("name").asString()
                if (!name.isNullOrBlank()) UserIdPrincipal(name) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
            }
        }
    }

    // Routing
    routing {
        get("/") {
            call.respondText("Welcome to shop!", contentType = ContentType.Text.Plain)
        }

        get("/health") {
            call.respond(HttpStatusCode.OK, Health(HealthStatus.HEALTHY))
        }

        // main API routes
        routeApiV1("api/v1")
    }
}
