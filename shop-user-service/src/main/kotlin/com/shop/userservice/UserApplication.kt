package com.shop.userservice

import com.shop.userservice.config.*
import com.shop.userservice.config.di.userModule
import com.shop.userservice.web.api.v1.routeApiV1
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(userModule(environment.config))
    }
    val simpleJwt by inject<SimpleJWT>()

    val client = HttpClient(Apache)

    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = true })
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
        val sessionSerializer = object : SessionSerializer<JustSellSession> {
            override fun deserialize(text: String): JustSellSession =
                Json.decodeFromString(text)
            override fun serialize(session: JustSellSession): String =
                Json.encodeToString(session)
        }

        // Use the generic cookie DSL overload so the builder provides `transform(...)`
        cookie<JustSellSession>("oauthSampleSessionId") {
            // assign the custom serializer inside the builder
            serializer = sessionSerializer

            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))

            // optional hardening
            cookie.httpOnly = true // JS can't access the cookie (prevents XSS)
            cookie.secure = true // Only send over HTTPS
            cookie.extensions["SameSite"] = "Strict" // Prevent CSRF

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