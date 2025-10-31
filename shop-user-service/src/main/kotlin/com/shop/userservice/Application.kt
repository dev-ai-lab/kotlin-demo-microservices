package com.shop.userservice

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.shop.userservice.config.*
import com.shop.userservice.config.di.appModule
import com.shop.userservice.web.api.v1.routeApiV1
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = EngineMain.main(args)


@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val simpleJwt by inject<SimpleJWT>()

    install(Koin) {
        slf4jLogger()
        modules(appModule(environment.config))
    }

    val client = HttpClient(Apache)

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(simpleJwt.verifier)
            realm = "shop"
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrEmpty()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
            }
        }

        session<JustSellSession>("session-auth") {
            validate { session ->
                if (session.sessionId != null) {
                    UserIdPrincipal(session.sessionId)
                } else null
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Session missing or invalid token")
            }
        }

        oauth("google-oauth") {
            this.client = client
            providerLookup = { googleOauthProvider }
            urlProvider = { redirectUrl("/api/v1/google/login") }
        }
    }
    // Extract config value here, while `environment` is in scope
    val secretSignKey = hex(environment.config.property("shop.shop-user-service.cookies.sign_secret").getString())

    install(Sessions) {
        val mapper = jacksonObjectMapper()
        val sessionSerializer = object : SessionSerializer<JustSellSession> {
            override fun deserialize(text: String): JustSellSession =
                mapper.readValue(text, JustSellSession::class.java)

            override fun serialize(session: JustSellSession): String =
                mapper.writeValueAsString(session)
        }

        // Use the generic cookie DSL overload so the builder provides `transform(...)`
        cookie<JustSellSession>("oauthSampleSessionId") {
            // assign the custom serializer inside the builder
            serializer = sessionSerializer

            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))

            // optional hardening
            cookie.httpOnly = true
            cookie.secure = true
            cookie.extensions["SameSite"] = "Strict" // Prevent CSRF, XSS attacks

            // 💡 Set cookie age to 1 hour
            cookie.maxAgeInSeconds = 60 * 5 // 5 minutes
        }
    }

/*    install(CORS) {
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        header(HttpHeaders.Authorization)
        header(HttpHeaders.ContentType)
    }}*/

    routing {
        get("/") {
            call.respondText("Welcome to shop!", contentType = ContentType.Text.Plain)
        }

        get("/health") {
            call.respond(HttpStatusCode.OK, Health(HealthStatus.HEALTHY))
        }

        routeApiV1("api/v1")
    }
}