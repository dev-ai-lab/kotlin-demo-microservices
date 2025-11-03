package com.shop.userservice.web.api.v1

import com.shop.userservice.config.*
import com.shop.userservice.domain.*
import com.shop.userservice.web.api.v1.dto.ProfileCreateDto
import com.shop.userservice.web.api.v1.dto.UserSignupDto
import com.shop.userservice.web.api.v1.dto.request.RefreshTokenRequest
import com.shop.userservice.web.api.v1.dto.request.UserRequestDto
import com.shop.userservice.web.api.v1.dto.response.AuthResponse
import com.shop.userservice.web.api.v1.dto.response.ProfileInfo
import com.shop.userservice.web.api.v1.dto.response.UserSignupResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.ktor.ext.inject
import java.util.*
import kotlin.text.get

fun Route.routeApiV1(path: String) = route(path) {
    val simpleJwt by inject<SimpleJWT>()

    post("/signup") {
        val signup = call.receive<UserSignupDto>()

        // Validate input
        if (signup.username.isBlank() || signup.password.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, "Username and password must not be empty")
        }

        if (users.values.any { it.username == signup.username }) {
            return@post call.respond(HttpStatusCode.Conflict, "Username already exists")
        }

        // Hash password
        val passwordHash = hashPassword(signup.password)

        // Create user
        val userId = UUID.randomUUID()
        val user = User(
            userId, username = signup.username, passwordHash = passwordHash)
        users[userId.toString()] = user

        // Create default profile linked to the userId
        val profile = Profile(userId = userId,
            firstname = "",
            lastname = "",
            email = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
            )
        profiles[userId] = profile
        // Respond with created profile info (excluding sensitive data)
        call.respond(
            HttpStatusCode.Created,
            UserSignupResponseDto(
                username = user.username,
                profile = ProfileInfo(
                    firstname = profile.firstname,
                    lastname = profile.lastname
                )
            )
        )
    }

    post("/login") {
        val userDto = call.receive<UserRequestDto>()

        // Look up user by username
        val user = users.values.find { it.username == userDto.username }
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")

        // Verify password
        if (!verifyPassword(userDto.password, user.passwordHash)) {
            return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }

        // Generate JWT using immutable userId
        val accessToken = simpleJwt.sign(user.id)

        // Generate refresh token and map to userId
        val refreshToken = UUID.randomUUID().toString()
        refreshTokens[refreshToken] = user.id.toString()

        // Respond with tokens (client only knows username, never userId)
        call.respond(AuthResponse(token = accessToken, refresh_token = refreshToken))
    }


    post("/token/refresh") {
        val request = call.receive<RefreshTokenRequest>()
        val userId = refreshTokens[request.refreshToken] ?: run {
            call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
            return@post
        }

        val newAccessToken = simpleJwt.sign(UUID.fromString(userId))
        call.respond(AuthResponse(token = newAccessToken))
    }

    authenticate("jwt", "session-auth") {
        route("/profile") {
            get {
                val userId = call.currentUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                //val sellSession = call.sessions.get<JustSellSession>()

                val profile = profiles[UUID.fromString(userId)] ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(profile)
            }

            post {
                val userId = call.currentUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<ProfileCreateDto>()
                val profile = Profile(
                    userId = UUID.fromString(userId),
                    firstname = request.firstname,
                    lastname = request.lastname,
                    email = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                profiles[UUID.fromString(userId)] = profile

                call.respond(profile)
            }

            // PUT: Update the logged-in user's profile
            put {
                val userId = call.currentUserId()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val existingProfile = profiles[UUID.fromString(userId)]
                    ?: return@put call.respond(HttpStatusCode.NotFound, "Profile not found")

                val updatedProfileDto = call.receive<ProfileCreateDto>()

                // Only update safe fields from DTO
                existingProfile.firstname = updatedProfileDto.firstname
                existingProfile.lastname = updatedProfileDto.lastname

                profiles[UUID.fromString(userId)] = existingProfile

                call.respond(existingProfile)
            }
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

                val jsonElement = Json.parseToJsonElement(json)
                val id = jsonElement.jsonObject["id"]?.jsonPrimitive?.content

                if (id != null) {
                    call.sessions.set(JustSellSession(id))
                }
                call.respondRedirect("/")
            }
        }
    }
}
