package com.shop.userservice.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.request.*

// Note: avoid importing non-existent oauth package for this Ktor version.

class SimpleJWT(secret: String){
    val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()

    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

val simpleJwt = SimpleJWT("my-super-secret-for-jwt")

var googleOauthProvider = OAuthServerSettings.OAuth2ServerSettings(
    name = "google",
    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
    accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
    requestMethod = HttpMethod.Post,
    clientId = "xxxclientIdxxx",
    clientSecret = "xxxclientSecret",
    defaultScopes = listOf("profile")
)

fun ApplicationCall.redirectUrl(path: String): String {
    val origin = this.request.local
    val defaultPort = if (origin.scheme == "http") 80 else 443
    val hostPort = origin.localHost + origin.localPort.let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = origin.scheme
    return "$protocol://$hostPort$path"
}

data class JustSellSession(val notUserId: String)
