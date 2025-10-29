package com.shop.userservice

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.shop.userservice.config.*
import com.shop.userservice.web.api.v1.routeApiV1
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
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

        oauth("google-oauth") {
            this.client = client
            providerLookup = { googleOauthProvider }
            urlProvider = { redirectUrl("/api/v1/login")}
        }
    }
    install(Sessions) {
        // Use a JSON serializer for sessions to avoid requiring kotlinx.serialization.
        val mapper = jacksonObjectMapper()
        cookie<JustSellSession>("oauthSampleSessionId") {
            serializer = object : SessionSerializer<JustSellSession> {
                override fun deserialize(text: String): JustSellSession = mapper.readValue(text, JustSellSession::class.java)
                override fun serialize(session: JustSellSession): String = mapper.writeValueAsString(session)
            }
            val secretSignKey = hex("0001020304bbbb090a0b0c0d0e0f") // @TODO: Remember to change this!
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
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
}
