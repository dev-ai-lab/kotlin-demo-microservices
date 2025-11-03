package com.shop.userservice.web.api.v1

import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RouteApiV1IntTest {
    private val json = Json { ignoreUnknownKeys = true }

    // Helper: sign up a new user
    private suspend fun signupUser(client: HttpClient, username: String, password: String) {
        val signupResponse = client.post("/api/v1/signup") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {"username":"$username","password":"$password"}
                """.trimIndent()
            )
        }
        assertTrue(
            signupResponse.status == HttpStatusCode.Created || signupResponse.status == HttpStatusCode.OK,
            "expected 200 or 201 for signup but was ${signupResponse.status}"
        )
    }

    // Helper: login and return Pair(accessToken, refreshToken)
    private suspend fun loginAndGetTokens(client: HttpClient, username: String, password: String): Pair<String, String> {
        val loginResponse = client.post("/api/v1/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$username","password":"$password"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status, "login should return 200")

        val loginBody = loginResponse.bodyAsText()
        val loginJson = json.parseToJsonElement(loginBody).jsonObject

        val accessToken = findToken(loginJson, listOf("token", "accessToken", "access_token", "access"))
        val refreshToken = findToken(loginJson, listOf("refreshToken", "refresh_token", "refresh"))

        assertNotNull(accessToken, "access token not found in login response: $loginBody")
        assertNotNull(refreshToken, "refresh token not found in login response: $loginBody")

        return Pair(accessToken, refreshToken)
    }

    private fun findToken(obj: kotlinx.serialization.json.JsonObject, keys: List<String>): String? {
        for (k in keys) {
            obj[k]?.let { return it.jsonPrimitive.content }
        }
        obj.entries.firstOrNull { it.key.contains("token", true) }?.let { return it.value.jsonPrimitive.content }
        return null
    }

    @Test
    fun testSignUp() = testApplication {
        environment { config = HoconApplicationConfig(ConfigFactory.load()) }

        val username = "itest_signup_${System.currentTimeMillis()}"
        val password = "Password123!"

        signupUser(client, username, password)
    }

    @Test
    fun testLogin() = testApplication {
        environment { config = HoconApplicationConfig(ConfigFactory.load()) }

        val username = "itest_login_${System.currentTimeMillis()}"
        val password = "Password123!"

        signupUser(client, username, password)
        val (access, refresh) = loginAndGetTokens(client, username, password)
        assertTrue(access.isNotBlank())
        assertTrue(refresh.isNotBlank())
    }

    @Test
    fun testGetProfile() = testApplication {
        environment { config = HoconApplicationConfig(ConfigFactory.load()) }

        val username = "itest_profile_get_${System.currentTimeMillis()}"
        val password = "Password123!"

        signupUser(client, username, password)
        val (access, _) = loginAndGetTokens(client, username, password)

        val profileGetResponse = client.get("/api/v1/profile") {
            header(HttpHeaders.Authorization, "Bearer $access")
        }
        assertEquals(HttpStatusCode.OK, profileGetResponse.status)
        val profileGetBody = profileGetResponse.bodyAsText()
        val profileGetJson = json.parseToJsonElement(profileGetBody).jsonObject
        assertTrue(profileGetJson.containsKey("firstname") || profileGetJson.containsKey("username"))
    }

    @Test
    fun testUpdateProfile() = testApplication {
        environment { config = HoconApplicationConfig(ConfigFactory.load()) }

        val username = "itest_profile_update_${System.currentTimeMillis()}"
        val password = "Password123!"

        signupUser(client, username, password)
        val (access, _) = loginAndGetTokens(client, username, password)

        val updatedFirst = "Jane"
        val updatedLast = "Smith"
        val profilePostResponse = client.post("/api/v1/profile") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $access")
            setBody("""{"firstname":"$updatedFirst","lastname":"$updatedLast"}""")
        }
        assertEquals(HttpStatusCode.OK, profilePostResponse.status)
        val profilePostBody = profilePostResponse.bodyAsText()
        val profilePostJson = json.parseToJsonElement(profilePostBody).jsonObject
        assertEquals(updatedFirst, profilePostJson["firstname"]?.jsonPrimitive?.content)
        assertEquals(updatedLast, profilePostJson["lastname"]?.jsonPrimitive?.content)
    }

    @Test
    fun testRefreshToken() = testApplication {
        environment { config = HoconApplicationConfig(ConfigFactory.load()) }

        val username = "itest_refresh_${System.currentTimeMillis()}"
        val password = "Password123!"

        signupUser(client, username, password)
        val (_, refresh) = loginAndGetTokens(client, username, password)

        val refreshResponse = client.post("/api/v1/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":"$refresh"}""")
        }
        assertEquals(HttpStatusCode.OK, refreshResponse.status)
        val refreshBody = refreshResponse.bodyAsText()
        val refreshJson = json.parseToJsonElement(refreshBody).jsonObject
        val newAccessToken = findToken(refreshJson, listOf("token", "accessToken", "access_token", "access"))
        assertNotNull(newAccessToken, "new access token not found in refresh response: $refreshBody")
    }
}
