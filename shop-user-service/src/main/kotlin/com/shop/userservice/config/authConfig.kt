package com.shop.userservice.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.password4j.Hash
import com.password4j.Password
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

// Note: avoid importing non-existent oauth package for this Ktor version.

class SimpleJWT(secret: String){
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm).build()

    fun sign(userId: UUID): String {
        val now = Date()
        val expiresAt = Date(now.time + 5 * 60 * 1000) // 5 minutes = 300,000 ms
        return JWT.create()
            .withClaim("userId", userId.toString())
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}

var googleOauthProvider = OAuthServerSettings.OAuth2ServerSettings(
    name = "google",
    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
    //accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
    accessTokenUrl = "https://oauth2.googleapis.com/token",
    requestMethod = HttpMethod.Post,
    clientId = "xxxx",
    clientSecret = "xxx",
    defaultScopes = listOf("profile", "email")
)

fun ApplicationCall.redirectUrl(path: String): String {
    val origin = this.request.local
    val defaultPort = if (origin.scheme == "http") 80 else 443
    val hostPort = origin.localHost + origin.localPort.let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = origin.scheme
    return "$protocol://$hostPort$path"
}

data class JustSellSession(val sessionId: String?)

val refreshTokens = mutableMapOf<String, String>() // token -> userId mapping

fun hashPassword(password: String): String {
    // Use default Argon2id parameters (secure and salt is handled internally)
    val hash: Hash = Password.hash(password).withArgon2()
    return hash.result
}

fun verifyPassword(plain: String, hashed: String): Boolean {
    return Password.check(plain, hashed).withArgon2()
}

fun ApplicationCall.currentUserId(): String? {
    return principal<JWTPrincipal>()?.getClaim("userId", String::class)
        ?: principal<UserIdPrincipal>()?.name
}




