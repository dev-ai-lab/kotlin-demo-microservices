package com.shop.userservice

import com.fasterxml.jackson.databind.SerializationFeature
import com.shop.userservice.config.*
//import com.shop.userservice.web.api.v1.routeApiV1
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.server.plugins.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.slf4j.event.Level

/*fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    //configureRouting()
}*/
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
 */

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

        oauth("google-oauth") {
            this.client = client
            providerLookup = { googleOauthProvider }
            urlProvider = { redirectUrl("/api/v1/login")}
        }
    }
    //install(WebSockets)
    install(Sessions) {
        cookie<JustSellSession>("oauthSampleSessionId") {
            val secretSignKey = hex("0001020304bbbb090a0b0c0d0e0f") // @TODO: Remember to change this!
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }
    install(CallLogging) {
        level = Level.ERROR
        //filter { call -> call.request.path().startsWith("/section1") }
        //filter { call -> call.request.path().startsWith("/section2") }
        // ...
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
